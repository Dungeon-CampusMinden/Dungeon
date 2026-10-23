-- Audience-mode votes: the counted vote of one participant in a voting window, and the window result
-- with the vote distribution and the executed action or adopted answer.
ALTER TABLE tracking_events
    DROP CONSTRAINT tracking_events_event_type_check,
    ADD CONSTRAINT tracking_events_event_type_check CHECK (event_type IN (
        'PARTICIPANT_JOINED', 'PARTICIPANT_LEFT', 'PUZZLE_STARTED', 'ANSWER_SUBMITTED',
        'INTERACTION_RECORDED', 'HINT_USED', 'PUZZLE_SOLVED',
        'PLAY_STARTED', 'PLAY_PAUSED', 'PLAY_RESUMED', 'PLAY_ENDED', 'SURVEY_ANSWERED',
        'BUG_REPORTED', 'VOTE_CAST', 'VOTE_RESOLVED')),
    DROP CONSTRAINT tracking_events_participant_presence_check,
    ADD CONSTRAINT tracking_events_participant_presence_check
    CHECK ((event_type IN ('PARTICIPANT_JOINED', 'PARTICIPANT_LEFT', 'ANSWER_SUBMITTED',
        'INTERACTION_RECORDED', 'HINT_USED', 'SURVEY_ANSWERED', 'BUG_REPORTED', 'VOTE_CAST'))
        = (participant_id IS NOT NULL)),
    ADD CONSTRAINT tracking_events_vote_payload_check
    CHECK (event_type NOT IN ('VOTE_CAST', 'VOTE_RESOLVED') OR COALESCE(
        jsonb_typeof(payload -> 'window') = 'number'
        AND jsonb_typeof(payload -> 'context') = 'string'
        AND (event_type <> 'VOTE_CAST'
            OR (payload ? 'vote' AND jsonb_typeof(payload -> 'vote') <> 'null'))
        AND (event_type <> 'VOTE_RESOLVED'
            OR (payload ? 'result' AND jsonb_typeof(payload -> 'votes') = 'array')), false));
