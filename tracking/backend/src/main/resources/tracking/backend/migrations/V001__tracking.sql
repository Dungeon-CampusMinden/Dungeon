CREATE TABLE IF NOT EXISTS tracking_sessions (
    session_id UUID PRIMARY KEY,
    schema_version INTEGER NOT NULL CHECK (schema_version >= 1),
    room_id TEXT NOT NULL CHECK (room_id <> ''),
    started_at TIMESTAMPTZ NOT NULL,
    status TEXT NOT NULL DEFAULT 'RUNNING'
        CONSTRAINT tracking_sessions_status_check
        CHECK (status IN ('RUNNING', 'COMPLETED', 'ABORTED')),
    ended_at TIMESTAMPTZ,
    finish_elapsed_ms BIGINT CHECK (finish_elapsed_ms >= 0),
    final_sequence BIGINT CHECK (final_sequence >= 0),
    aborted_at_puzzle_id TEXT,
    CONSTRAINT tracking_sessions_lifecycle_check
    CHECK ((status = 'RUNNING' AND ended_at IS NULL AND finish_elapsed_ms IS NULL
        AND final_sequence IS NULL AND aborted_at_puzzle_id IS NULL)
        OR (status IN ('COMPLETED', 'ABORTED') AND ended_at IS NOT NULL
            AND finish_elapsed_ms IS NOT NULL AND final_sequence IS NOT NULL)),
    CONSTRAINT tracking_sessions_end_time_check
    CHECK (ended_at IS NULL OR ended_at >= started_at),
    CONSTRAINT tracking_sessions_abort_puzzle_check
    CHECK (status = 'ABORTED' OR aborted_at_puzzle_id IS NULL)
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
    schema_version INTEGER NOT NULL CHECK (schema_version >= 1),
    participant_id UUID,
    room_id TEXT NOT NULL,
    event_type TEXT NOT NULL CHECK (event_type IN (
        'PARTICIPANT_JOINED', 'PARTICIPANT_LEFT', 'PUZZLE_STARTED', 'ANSWER_SUBMITTED',
        'HINT_USED', 'PUZZLE_SOLVED')),
    puzzle_id TEXT,
    object_id TEXT,
    outcome TEXT,
    elapsed_monotonic_ms BIGINT NOT NULL CHECK (elapsed_monotonic_ms >= 0),
    occurred_at TIMESTAMPTZ NOT NULL,
    payload JSONB NOT NULL,
    event_json JSONB NOT NULL,
    persisted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (session_id, session_sequence),
    FOREIGN KEY (session_id, participant_id)
        REFERENCES tracking_participants(session_id, participant_id),
    CHECK ((event_type = 'ANSWER_SUBMITTED' AND outcome IN ('CORRECT', 'INCORRECT')
        AND object_id IS NOT NULL
        AND payload ? 'answer' AND payload -> 'answer' <> 'null'::jsonb
        AND jsonb_typeof(payload -> 'answerKind') = 'string'
        AND btrim(payload ->> 'answerKind') <> ''
        AND jsonb_typeof(payload -> 'attemptNumber') = 'number'
        AND payload ->> 'attemptNumber' ~ '^[1-9][0-9]*$')
        OR (event_type <> 'ANSWER_SUBMITTED' AND outcome IS NULL)),
    CHECK (event_type <> 'HINT_USED' OR object_id IS NOT NULL),
    CHECK ((event_type IN ('PUZZLE_STARTED', 'ANSWER_SUBMITTED', 'HINT_USED', 'PUZZLE_SOLVED'))
        = (puzzle_id IS NOT NULL)),
    CHECK ((event_type IN ('PARTICIPANT_JOINED', 'PARTICIPANT_LEFT', 'ANSWER_SUBMITTED',
        'HINT_USED')) = (participant_id IS NOT NULL))
);

CREATE INDEX IF NOT EXISTS tracking_events_session_puzzle_sequence_idx
    ON tracking_events(session_id, puzzle_id, session_sequence);

CREATE OR REPLACE VIEW v_session_summary AS
SELECT
    s.session_id,
    s.room_id,
    s.started_at,
    s.ended_at,
    s.status,
    (SELECT count(*) FROM tracking_participants p WHERE p.session_id = s.session_id)
        AS actual_player_count,
    COALESCE(s.finish_elapsed_ms,
        (SELECT max(e.elapsed_monotonic_ms) FROM tracking_events e
            WHERE e.session_id = s.session_id), 0) AS duration_ms,
    s.aborted_at_puzzle_id AS aborted_at_puzzle_id
FROM tracking_sessions s;

CREATE OR REPLACE VIEW v_puzzle_summary AS
WITH session_ends AS (
    SELECT
        s.session_id,
        COALESCE(s.finish_elapsed_ms, max(e.elapsed_monotonic_ms), 0) AS end_elapsed_ms
    FROM tracking_sessions s
    LEFT JOIN tracking_events e ON e.session_id = s.session_id
    GROUP BY s.session_id, s.finish_elapsed_ms
), puzzle_events AS (
    SELECT
        session_id,
        puzzle_id,
        COALESCE(min(elapsed_monotonic_ms) FILTER (WHERE event_type = 'PUZZLE_STARTED'),
            min(elapsed_monotonic_ms)) AS first_event_elapsed_ms,
        min(elapsed_monotonic_ms) FILTER (WHERE event_type = 'PUZZLE_SOLVED')
            AS solved_elapsed_ms,
        count(*) FILTER (WHERE event_type = 'ANSWER_SUBMITTED') AS attempt_count,
        count(*) FILTER (WHERE event_type = 'HINT_USED') AS hint_count
    FROM tracking_events
    WHERE puzzle_id IS NOT NULL
    GROUP BY session_id, puzzle_id
)
SELECT
    p.session_id,
    p.puzzle_id,
    p.first_event_elapsed_ms,
    p.solved_elapsed_ms,
    GREATEST(COALESCE(p.solved_elapsed_ms, s.end_elapsed_ms) - p.first_event_elapsed_ms, 0)
        AS duration_ms,
    p.solved_elapsed_ms IS NOT NULL AS solved,
    p.attempt_count,
    p.hint_count
FROM puzzle_events p
JOIN session_ends s ON s.session_id = p.session_id;

CREATE OR REPLACE VIEW v_attempts_answers AS
SELECT
    session_id,
    session_sequence,
    participant_id,
    puzzle_id,
    object_id,
    event_type,
    outcome,
    occurred_at,
    elapsed_monotonic_ms,
    payload ->> 'answerKind' AS answer_kind,
    payload ->> 'answer' AS answer,
    (payload ->> 'attemptNumber')::INTEGER AS attempt_number,
    payload
FROM tracking_events
WHERE event_type = 'ANSWER_SUBMITTED';
