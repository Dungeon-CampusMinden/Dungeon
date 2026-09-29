ALTER TABLE tracking_events
    DROP CONSTRAINT tracking_events_event_type_check,
    DROP CONSTRAINT tracking_events_check2,
    DROP CONSTRAINT tracking_events_check3;

ALTER TABLE tracking_events
    ADD CONSTRAINT tracking_events_event_type_check CHECK (event_type IN (
        'PARTICIPANT_JOINED', 'PARTICIPANT_LEFT', 'PUZZLE_STARTED', 'ANSWER_SUBMITTED',
        'INTERACTION_RECORDED', 'HINT_USED', 'PUZZLE_SOLVED')),
    ADD CONSTRAINT tracking_events_puzzle_presence_check CHECK (
        (event_type IN ('PUZZLE_STARTED', 'ANSWER_SUBMITTED', 'INTERACTION_RECORDED',
            'HINT_USED', 'PUZZLE_SOLVED')) = (puzzle_id IS NOT NULL)),
    ADD CONSTRAINT tracking_events_participant_presence_check CHECK (
        (event_type IN ('PARTICIPANT_JOINED', 'PARTICIPANT_LEFT', 'ANSWER_SUBMITTED',
            'INTERACTION_RECORDED', 'HINT_USED')) = (participant_id IS NOT NULL)),
    ADD CONSTRAINT tracking_events_interaction_payload_check CHECK (
        event_type <> 'INTERACTION_RECORDED' OR (
            outcome IS NULL
            AND object_id IS NOT NULL
            AND COALESCE(jsonb_typeof(payload -> 'action') = 'string'
                AND btrim(payload ->> 'action') <> '', false)
            AND COALESCE((payload ->> 'status') IN ('COMPLETED', 'BLOCKED', 'CANCELLED'), false)
            AND COALESCE(jsonb_typeof(payload -> 'reason') = 'string'
                AND btrim(payload ->> 'reason') <> '', false)));

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
        min(elapsed_monotonic_ms) FILTER (
            WHERE event_type IN ('PUZZLE_STARTED', 'PUZZLE_SOLVED'))
            AS first_event_elapsed_ms,
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
JOIN session_ends s ON s.session_id = p.session_id
WHERE p.first_event_elapsed_ms IS NOT NULL;
