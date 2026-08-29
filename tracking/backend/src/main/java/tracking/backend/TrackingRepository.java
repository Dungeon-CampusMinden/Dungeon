package tracking.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import tracking.core.TrackingAck;
import tracking.core.TrackingBatch;
import tracking.core.TrackingEvent;
import tracking.core.TrackingJson;
import tracking.core.TrackingParticipant;
import tracking.core.TrackingSessionDescriptor;
import tracking.core.TrackingSessionFinish;

final class TrackingRepository {
  private final Database database;

  TrackingRepository(final Database database) {
    this.database = database;
  }

  boolean healthy() {
    try (Connection connection = database.connect();
        PreparedStatement statement = connection.prepareStatement("SELECT 1");
        ResultSet result = statement.executeQuery()) {
      return result.next();
    } catch (SQLException exception) {
      return false;
    }
  }

  TrackingAck ingest(final TrackingBatch batch) throws SQLException {
    try (Connection connection = database.connect()) {
      connection.setAutoCommit(false);
      try {
        upsertSession(connection, batch.session());
        SessionState state = lockSession(connection, batch.session().sessionId());
        long persistedBefore = state.lastSequence();
        if (!batch.events().isEmpty()
            && batch.events().getFirst().sessionSequence() > persistedBefore + 1) {
          throw new RepositoryConflictException(
              "Batch starts after the next expected session sequence");
        }
        if (state.finished()
            && batch.events().stream()
                .anyMatch(event -> event.sessionSequence() > persistedBefore)) {
          throw new RepositoryConflictException("Finished sessions do not accept new events");
        }
        for (TrackingParticipant participant : batch.participants()) {
          if (state.finished()) {
            requireSameParticipant(connection, participant);
          } else {
            upsertParticipant(connection, participant);
          }
        }
        int accepted = 0;
        for (TrackingEvent event : batch.events()) {
          accepted += insertEvent(connection, event);
        }
        long persistedAfter = lastSequence(connection, batch.session().sessionId());
        connection.commit();
        return new TrackingAck(
            batch.schemaVersion(), batch.session().sessionId(), persistedAfter, accepted);
      } catch (RuntimeException | SQLException exception) {
        connection.rollback();
        throw exception;
      }
    }
  }

  TrackingAck ack(final UUID sessionId) throws SQLException {
    try (Connection connection = database.connect();
        PreparedStatement statement =
            connection.prepareStatement(
                "SELECT schema_version, COALESCE((SELECT max(session_sequence) "
                    + "FROM tracking_events WHERE session_id = ?), 0) "
                    + "FROM tracking_sessions WHERE session_id = ?")) {
      statement.setObject(1, sessionId);
      statement.setObject(2, sessionId);
      try (ResultSet result = statement.executeQuery()) {
        if (!result.next()) {
          throw new NoSuchElementException("Unknown tracking session");
        }
        return new TrackingAck(result.getInt(1), sessionId, result.getLong(2), 0);
      }
    }
  }

  TrackingAck finish(final TrackingSessionFinish finish) throws SQLException {
    try (Connection connection = database.connect()) {
      connection.setAutoCommit(false);
      try {
        SessionState state = lockSession(connection, finish.sessionId());
        if (state.finished()) {
          if (!sameFinish(connection, finish)) {
            throw new RepositoryConflictException("Session was already finished with other values");
          }
          connection.commit();
          return new TrackingAck(
              finish.schemaVersion(), finish.sessionId(), state.lastSequence(), 0);
        }
        SequenceState sequences = sequenceState(connection, finish.sessionId());
        if (sequences.lastSequence() != finish.finalSequence()
            || sequences.eventCount() != finish.finalSequence()) {
          throw new RepositoryConflictException(
              "Session does not contain every event through finalSequence");
        }
        int updated;
        try (PreparedStatement statement =
            connection.prepareStatement(
                "UPDATE tracking_sessions SET status = ?, ended_at = ?, finish_elapsed_ms = ?, "
                    + "final_sequence = ?, aborted_at_puzzle_id = ? "
                    + "WHERE session_id = ? AND schema_version = ? AND status IS NULL "
                    + "AND started_at <= ?")) {
          statement.setString(1, finish.status().name());
          statement.setTimestamp(2, Timestamp.from(finish.endedAt()));
          statement.setLong(3, finish.elapsedMonotonicMs());
          statement.setLong(4, finish.finalSequence());
          optionalText(statement, 5, finish.abortedAtPuzzleId());
          statement.setObject(6, finish.sessionId());
          statement.setInt(7, finish.schemaVersion());
          statement.setTimestamp(8, Timestamp.from(finish.endedAt()));
          updated = statement.executeUpdate();
        }
        if (updated != 1) {
          throw new RepositoryConflictException("Session descriptor does not match finish request");
        }
        connection.commit();
        return new TrackingAck(
            finish.schemaVersion(), finish.sessionId(), finish.finalSequence(), 0);
      } catch (RuntimeException | SQLException exception) {
        connection.rollback();
        throw exception;
      }
    }
  }

  private static void upsertSession(
      final Connection connection, final TrackingSessionDescriptor session) throws SQLException {
    int inserted;
    try (PreparedStatement statement =
        connection.prepareStatement(
            "INSERT INTO tracking_sessions(session_id, schema_version, room_id, "
                + "started_at) VALUES (?, ?, ?, ?) ON CONFLICT (session_id) DO NOTHING")) {
      statement.setObject(1, session.sessionId());
      statement.setInt(2, session.schemaVersion());
      statement.setString(3, session.roomId());
      statement.setTimestamp(4, Timestamp.from(session.startedAt()));
      inserted = statement.executeUpdate();
    }
    if (inserted == 0 && !sameSession(connection, session)) {
      throw new RepositoryConflictException("Session ID already has another descriptor");
    }
  }

  private static boolean sameSession(
      final Connection connection, final TrackingSessionDescriptor session) throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "SELECT schema_version = ? AND room_id = ? AND started_at = ? "
                + "FROM tracking_sessions WHERE session_id = ?")) {
      statement.setInt(1, session.schemaVersion());
      statement.setString(2, session.roomId());
      statement.setTimestamp(3, Timestamp.from(session.startedAt()));
      statement.setObject(4, session.sessionId());
      try (ResultSet result = statement.executeQuery()) {
        return result.next() && result.getBoolean(1);
      }
    }
  }

  private static void upsertParticipant(
      final Connection connection, final TrackingParticipant participant) throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "INSERT INTO tracking_participants(session_id, participant_id, room_played_before) "
                + "VALUES (?, ?, ?) "
                + "ON CONFLICT (session_id, participant_id) DO UPDATE SET "
                + "room_played_before = EXCLUDED.room_played_before "
                + "WHERE tracking_participants.room_played_before = EXCLUDED.room_played_before")) {
      statement.setObject(1, participant.sessionId());
      statement.setObject(2, participant.participantId());
      statement.setBoolean(3, participant.roomPlayedBefore());
      if (statement.executeUpdate() != 1) {
        throw new RepositoryConflictException(
            "Participant ID already has other session-scoped facts");
      }
    }
  }

  private static void requireSameParticipant(
      final Connection connection, final TrackingParticipant participant) throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "SELECT room_played_before = ? FROM tracking_participants "
                + "WHERE session_id = ? AND participant_id = ?")) {
      statement.setBoolean(1, participant.roomPlayedBefore());
      statement.setObject(2, participant.sessionId());
      statement.setObject(3, participant.participantId());
      try (ResultSet result = statement.executeQuery()) {
        if (!result.next() || !result.getBoolean(1)) {
          throw new RepositoryConflictException(
              "Finished sessions accept only identical participant repetitions");
        }
      }
    }
  }

  private static int insertEvent(final Connection connection, final TrackingEvent event)
      throws SQLException {
    String eventJson = TrackingJson.write(event);
    int inserted;
    try (PreparedStatement statement =
        connection.prepareStatement(
            "INSERT INTO tracking_events(session_id, session_sequence, event_id, schema_version, "
                + "participant_id, room_id, event_type, puzzle_id, object_id, outcome, "
                + "elapsed_monotonic_ms, occurred_at, payload, event_json) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb) "
                + "ON CONFLICT DO NOTHING")) {
      statement.setObject(1, event.sessionId());
      statement.setLong(2, event.sessionSequence());
      statement.setString(3, event.eventId());
      statement.setInt(4, event.schemaVersion());
      optionalUuid(statement, 5, event.participantId());
      statement.setString(6, event.roomId());
      statement.setString(7, event.eventType().name());
      optionalText(statement, 8, event.puzzleId());
      optionalText(statement, 9, event.objectId());
      if (event.outcome().isPresent()) {
        statement.setString(10, event.outcome().orElseThrow().name());
      } else {
        statement.setNull(10, Types.VARCHAR);
      }
      statement.setLong(11, event.elapsedMonotonicMs());
      statement.setTimestamp(12, Timestamp.from(event.occurredAt()));
      statement.setString(13, event.payload().toString());
      statement.setString(14, eventJson);
      inserted = statement.executeUpdate();
    }
    if (inserted == 0 && !sameEvent(connection, event, eventJson)) {
      throw new RepositoryConflictException(
          "Event sequence or event ID conflicts with persisted data");
    }
    return inserted;
  }

  private static boolean sameEvent(
      final Connection connection, final TrackingEvent event, final String eventJson)
      throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "SELECT event_json = ?::jsonb FROM tracking_events "
                + "WHERE session_id = ? AND session_sequence = ?")) {
      statement.setString(1, eventJson);
      statement.setObject(2, event.sessionId());
      statement.setLong(3, event.sessionSequence());
      try (ResultSet result = statement.executeQuery()) {
        return result.next() && result.getBoolean(1);
      }
    }
  }

  private static SessionState lockSession(final Connection connection, final UUID sessionId)
      throws SQLException {
    try (PreparedStatement lock =
        connection.prepareStatement(
            "SELECT status FROM tracking_sessions WHERE session_id = ? FOR UPDATE")) {
      lock.setObject(1, sessionId);
      try (ResultSet result = lock.executeQuery()) {
        if (!result.next()) {
          throw new NoSuchElementException("Unknown tracking session");
        }
        return new SessionState(result.getString(1) != null, lastSequence(connection, sessionId));
      }
    }
  }

  private static SequenceState sequenceState(final Connection connection, final UUID sessionId)
      throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "SELECT count(*), COALESCE(max(session_sequence), 0) "
                + "FROM tracking_events WHERE session_id = ?")) {
      statement.setObject(1, sessionId);
      try (ResultSet result = statement.executeQuery()) {
        result.next();
        return new SequenceState(result.getLong(1), result.getLong(2));
      }
    }
  }

  private static long lastSequence(final Connection connection, final UUID sessionId)
      throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "SELECT COALESCE(max(session_sequence), 0) FROM tracking_events WHERE session_id = ?")) {
      statement.setObject(1, sessionId);
      try (ResultSet result = statement.executeQuery()) {
        result.next();
        return result.getLong(1);
      }
    }
  }

  private static boolean sameFinish(final Connection connection, final TrackingSessionFinish finish)
      throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "SELECT schema_version = ? AND status = ? AND ended_at = ? "
                + "AND finish_elapsed_ms = ? AND final_sequence = ? "
                + "AND aborted_at_puzzle_id IS NOT DISTINCT FROM ? "
                + "FROM tracking_sessions WHERE session_id = ?")) {
      statement.setInt(1, finish.schemaVersion());
      statement.setString(2, finish.status().name());
      statement.setTimestamp(3, Timestamp.from(finish.endedAt()));
      statement.setLong(4, finish.elapsedMonotonicMs());
      statement.setLong(5, finish.finalSequence());
      optionalText(statement, 6, finish.abortedAtPuzzleId());
      statement.setObject(7, finish.sessionId());
      try (ResultSet result = statement.executeQuery()) {
        return result.next() && result.getBoolean(1);
      }
    }
  }

  private static void optionalText(
      final PreparedStatement statement, final int index, final Optional<String> value)
      throws SQLException {
    if (value.isPresent()) {
      statement.setString(index, value.orElseThrow());
    } else {
      statement.setNull(index, Types.VARCHAR);
    }
  }

  private static void optionalUuid(
      final PreparedStatement statement, final int index, final Optional<UUID> value)
      throws SQLException {
    if (value.isPresent()) {
      statement.setObject(index, value.orElseThrow());
    } else {
      statement.setNull(index, Types.OTHER);
    }
  }

  static final class RepositoryConflictException extends IllegalStateException {
    RepositoryConflictException(final String message) {
      super(message);
    }
  }

  private record SessionState(boolean finished, long lastSequence) {}

  private record SequenceState(long eventCount, long lastSequence) {}
}
