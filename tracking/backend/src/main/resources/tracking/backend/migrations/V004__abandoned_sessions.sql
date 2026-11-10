-- A running session whose game ended without sending its finish shows as ABANDONED: its run has a
-- later session, or the backend stored nothing from it for 24 hours. The table row stays RUNNING,
-- so a finish that still arrives is accepted and replaces the derived status.
CREATE OR REPLACE VIEW v_session_summary AS
SELECT
    s.session_id,
    s.run_id,
    s.room_id,
    s.started_at,
    s.ended_at,
    CASE WHEN s.status = 'RUNNING' AND (
            EXISTS (SELECT 1 FROM tracking_sessions later
                WHERE later.run_id = s.run_id AND later.started_at > s.started_at)
            OR COALESCE((SELECT max(e.persisted_at) FROM tracking_events e
                WHERE e.session_id = s.session_id), s.started_at) < now() - interval '24 hours')
        THEN 'ABANDONED'
        ELSE s.status
    END AS status,
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
