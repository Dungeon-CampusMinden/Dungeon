CREATE TABLE IF NOT EXISTS tracking_sessions (
    session_id UUID PRIMARY KEY,
    schema_version INTEGER NOT NULL CHECK (schema_version = 1),
    room_id TEXT NOT NULL CHECK (room_id <> ''),
    started_at TIMESTAMPTZ NOT NULL,
    run_id UUID NOT NULL,
    resumed_at_active_ms BIGINT NOT NULL CHECK (resumed_at_active_ms >= 0),
    status TEXT NOT NULL DEFAULT 'RUNNING'
        CONSTRAINT tracking_sessions_status_check
        CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED', 'INTERRUPTED')),
    ended_at TIMESTAMPTZ,
    finish_active_ms BIGINT CHECK (finish_active_ms >= resumed_at_active_ms),
    final_sequence BIGINT CHECK (final_sequence >= 0),
    interrupted_at_puzzle_id TEXT,
    CONSTRAINT tracking_sessions_lifecycle_check
    CHECK ((status = 'RUNNING' AND ended_at IS NULL AND finish_active_ms IS NULL
        AND final_sequence IS NULL AND interrupted_at_puzzle_id IS NULL)
        OR (status IN ('COMPLETED', 'FAILED', 'INTERRUPTED') AND ended_at IS NOT NULL
            AND finish_active_ms IS NOT NULL AND final_sequence IS NOT NULL)),
    CONSTRAINT tracking_sessions_end_time_check
    CHECK (ended_at IS NULL OR ended_at >= started_at),
    CONSTRAINT tracking_sessions_interruption_puzzle_check
    CHECK (status = 'INTERRUPTED' OR interrupted_at_puzzle_id IS NULL)
);

CREATE TABLE IF NOT EXISTS tracking_participants (
    session_id UUID NOT NULL REFERENCES tracking_sessions(session_id),
    participant_id UUID NOT NULL,
    room_played_before BOOLEAN NOT NULL,
    PRIMARY KEY (session_id, participant_id)
);

CREATE TABLE IF NOT EXISTS tracking_events (
    session_id UUID NOT NULL REFERENCES tracking_sessions(session_id),
    session_sequence BIGINT NOT NULL CHECK (session_sequence >= 1),
    schema_version INTEGER NOT NULL CHECK (schema_version = 1),
    participant_id UUID,
    room_id TEXT NOT NULL,
    event_type TEXT NOT NULL
        CONSTRAINT tracking_events_event_type_check CHECK (event_type IN (
        'PARTICIPANT_JOINED', 'PARTICIPANT_LEFT', 'PUZZLE_STARTED', 'ANSWER_SUBMITTED',
        'INTERACTION_RECORDED', 'HINT_USED', 'PUZZLE_SOLVED',
        'PLAY_STARTED', 'PLAY_PAUSED', 'PLAY_RESUMED', 'PLAY_ENDED', 'SURVEY_ANSWERED')),
    puzzle_id TEXT,
    object_id TEXT,
    outcome TEXT,
    active_ms BIGINT NOT NULL CHECK (active_ms >= 0),
    occurred_at TIMESTAMPTZ NOT NULL,
    payload JSONB NOT NULL,
    event_json JSONB NOT NULL,
    persisted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, session_sequence),
    FOREIGN KEY (session_id, participant_id)
        REFERENCES tracking_participants(session_id, participant_id),
    -- COALESCE turns missing values into a violation instead of an accepted NULL check.
    CONSTRAINT tracking_events_answer_check
    CHECK (COALESCE((event_type = 'ANSWER_SUBMITTED' AND outcome IN ('CORRECT', 'INCORRECT')
        AND object_id IS NOT NULL
        AND payload ? 'answer' AND payload -> 'answer' <> 'null'::jsonb
        AND jsonb_typeof(payload -> 'answerKind') = 'string'
        AND btrim(payload ->> 'answerKind') <> ''
        AND jsonb_typeof(payload -> 'attemptNumber') = 'number'
        AND payload ->> 'attemptNumber' ~ '^[1-9][0-9]*$')
        OR (event_type <> 'ANSWER_SUBMITTED' AND outcome IS NULL), false)),
    CONSTRAINT tracking_events_hint_object_check
    CHECK (event_type <> 'HINT_USED' OR object_id IS NOT NULL),
    CONSTRAINT tracking_events_interaction_payload_check
    CHECK (event_type <> 'INTERACTION_RECORDED' OR (
        object_id IS NOT NULL
        AND COALESCE(jsonb_typeof(payload -> 'action') = 'string'
            AND btrim(payload ->> 'action') <> '', false)
        AND COALESCE((payload ->> 'status') IN ('COMPLETED', 'BLOCKED', 'CANCELLED'), false)
        AND (NOT payload ? 'reason' OR COALESCE(jsonb_typeof(payload -> 'reason') = 'string'
            AND btrim(payload ->> 'reason') <> '', false)))),
    -- Interactions name their puzzle only when they belong to one.
    CONSTRAINT tracking_events_puzzle_presence_check
    CHECK (event_type = 'INTERACTION_RECORDED'
        OR (event_type IN ('PUZZLE_STARTED', 'ANSWER_SUBMITTED', 'HINT_USED', 'PUZZLE_SOLVED'))
            = (puzzle_id IS NOT NULL)),
    CONSTRAINT tracking_events_participant_presence_check
    CHECK ((event_type IN ('PARTICIPANT_JOINED', 'PARTICIPANT_LEFT', 'ANSWER_SUBMITTED',
        'INTERACTION_RECORDED', 'HINT_USED', 'SURVEY_ANSWERED')) = (participant_id IS NOT NULL)),
    CONSTRAINT tracking_events_pause_reason_check
    CHECK (event_type <> 'PLAY_PAUSED' OR COALESCE(
        payload ->> 'reason' IN ('PAUSE_DIALOG', 'PLAYERS_MISSING'), false)),
    CONSTRAINT tracking_events_survey_payload_check
    CHECK (event_type <> 'SURVEY_ANSWERED' OR COALESCE(
        jsonb_typeof(payload -> 'questionnaireId') = 'string'
        AND btrim(payload ->> 'questionnaireId') <> ''
        AND jsonb_typeof(payload -> 'questionId') = 'string'
        AND btrim(payload ->> 'questionId') <> ''
        AND payload ? 'answer' AND payload -> 'answer' <> 'null'::jsonb, false))
);

CREATE INDEX IF NOT EXISTS tracking_events_session_puzzle_sequence_idx
    ON tracking_events(session_id, puzzle_id, session_sequence);

CREATE OR REPLACE VIEW v_session_summary AS
SELECT
    s.session_id,
    s.run_id,
    s.room_id,
    s.started_at,
    s.ended_at,
    s.status,
    (SELECT count(*) FROM tracking_participants p WHERE p.session_id = s.session_id)
        AS actual_player_count,
    s.resumed_at_active_ms,
    COALESCE(s.finish_active_ms,
        (SELECT max(e.active_ms) FROM tracking_events e
            WHERE e.session_id = s.session_id), s.resumed_at_active_ms) AS active_ms,
    COALESCE(s.finish_active_ms,
        (SELECT max(e.active_ms) FROM tracking_events e
            WHERE e.session_id = s.session_id), s.resumed_at_active_ms)
        - s.resumed_at_active_ms AS duration_ms,
    s.interrupted_at_puzzle_id
FROM tracking_sessions s;

-- Events that still belong to a run's progress. A later session of the same run that resumed
-- from an earlier play time (loading a save) discards the events recorded after that point.
CREATE OR REPLACE VIEW v_run_events AS
SELECT s.run_id, e.*
FROM tracking_events e
JOIN tracking_sessions s ON s.session_id = e.session_id
WHERE NOT EXISTS (
    SELECT 1 FROM tracking_sessions later
    WHERE later.run_id = s.run_id
        AND later.started_at > s.started_at
        AND later.resumed_at_active_ms < e.active_ms);

-- One row per run. A session counts as completing the run only while no later session resumed
-- from before its end; the latest session states where an unfinished run stopped.
CREATE OR REPLACE VIEW v_run_summary AS
SELECT
    s.run_id,
    min(s.room_id) AS room_id,
    count(*) AS session_count,
    min(s.started_at) AS started_at,
    max(s.ended_at) AS ended_at,
    COALESCE(max((SELECT count(*) FROM tracking_participants p
        WHERE p.session_id = s.session_id)), 0) AS max_player_count,
    bool_or(s.status = 'COMPLETED' AND NOT EXISTS (
        SELECT 1 FROM tracking_sessions later
        WHERE later.run_id = s.run_id
            AND later.started_at > s.started_at
            AND later.resumed_at_active_ms < s.finish_active_ms)) AS completed,
    (SELECT latest.interrupted_at_puzzle_id FROM tracking_sessions latest
        WHERE latest.run_id = s.run_id
        ORDER BY latest.started_at DESC LIMIT 1) AS interrupted_at_puzzle_id,
    COALESCE((SELECT max(e.active_ms) FROM v_run_events e WHERE e.run_id = s.run_id), 0)
        AS active_ms
FROM tracking_sessions s
GROUP BY s.run_id;

CREATE OR REPLACE VIEW v_puzzle_summary AS
WITH puzzle_events AS (
    SELECT
        run_id,
        min(room_id) AS room_id,
        puzzle_id,
        min(active_ms) FILTER (WHERE event_type = 'PUZZLE_STARTED') AS unlocked_active_ms,
        min(active_ms) FILTER (WHERE event_type <> 'PUZZLE_STARTED') AS first_contact_active_ms,
        min(active_ms) FILTER (WHERE event_type = 'PUZZLE_SOLVED') AS solved_active_ms,
        count(*) FILTER (WHERE event_type = 'ANSWER_SUBMITTED') AS attempt_count,
        count(DISTINCT object_id) FILTER (WHERE event_type = 'HINT_USED') AS hint_count
    FROM v_run_events
    WHERE puzzle_id IS NOT NULL
    GROUP BY run_id, puzzle_id
)
SELECT
    run_id,
    room_id,
    puzzle_id,
    unlocked_active_ms,
    first_contact_active_ms,
    solved_active_ms,
    solved_active_ms - unlocked_active_ms AS unlocked_to_solved_ms,
    -- Blocked use before the unlock is no contact with an available puzzle.
    solved_active_ms - GREATEST(first_contact_active_ms, unlocked_active_ms)
        AS first_contact_to_solved_ms,
    solved_active_ms IS NOT NULL AS solved,
    attempt_count,
    hint_count
FROM puzzle_events;

CREATE OR REPLACE VIEW v_attempts_answers AS
SELECT
    run_id,
    session_id,
    session_sequence,
    participant_id,
    puzzle_id,
    object_id,
    event_type,
    outcome,
    occurred_at,
    active_ms,
    payload ->> 'answerKind' AS answer_kind,
    payload ->> 'answer' AS answer,
    (payload ->> 'attemptNumber')::INTEGER AS attempt_number,
    payload
FROM v_run_events
WHERE event_type = 'ANSWER_SUBMITTED';
