-- Bug reports are participant events whose payload carries the reported text.
ALTER TABLE tracking_events
    DROP CONSTRAINT tracking_events_event_type_check,
    ADD CONSTRAINT tracking_events_event_type_check CHECK (event_type IN (
        'PARTICIPANT_JOINED', 'PARTICIPANT_LEFT', 'PUZZLE_STARTED', 'ANSWER_SUBMITTED',
        'INTERACTION_RECORDED', 'HINT_USED', 'PUZZLE_SOLVED',
        'PLAY_STARTED', 'PLAY_PAUSED', 'PLAY_RESUMED', 'PLAY_ENDED', 'SURVEY_ANSWERED',
        'BUG_REPORTED')),
    DROP CONSTRAINT tracking_events_participant_presence_check,
    ADD CONSTRAINT tracking_events_participant_presence_check
    CHECK ((event_type IN ('PARTICIPANT_JOINED', 'PARTICIPANT_LEFT', 'ANSWER_SUBMITTED',
        'INTERACTION_RECORDED', 'HINT_USED', 'SURVEY_ANSWERED', 'BUG_REPORTED'))
        = (participant_id IS NOT NULL)),
    ADD CONSTRAINT tracking_events_bug_report_payload_check
    CHECK (event_type <> 'BUG_REPORTED' OR COALESCE(
        jsonb_typeof(payload -> 'text') = 'string'
        AND btrim(payload ->> 'text') <> '', false));
