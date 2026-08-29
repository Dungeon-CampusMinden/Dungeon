package tracking.backend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

final class MigrationRunner {
  private static final List<Migration> MIGRATIONS =
      List.of(new Migration("001", "/tracking/backend/migrations/V001__tracking.sql"));
  private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

  private MigrationRunner() {}

  static void migrate(final Database database, final Optional<String> runtimeRole)
      throws SQLException, IOException {
    try (Connection connection = database.connect()) {
      connection.setAutoCommit(false);
      try {
        createHistory(connection);
        for (Migration migration : MIGRATIONS) {
          if (!applied(connection, migration.version())) {
            try (Statement statement = connection.createStatement()) {
              statement.execute(readMigration(migration.resource()));
            }
            try (PreparedStatement statement =
                connection.prepareStatement(
                    "INSERT INTO tracking_schema_migrations(version) VALUES (?)")) {
              statement.setString(1, migration.version());
              statement.executeUpdate();
            }
          }
        }
        if (runtimeRole.isPresent()) {
          grantRuntimePrivileges(connection, runtimeRole.orElseThrow());
        }
        connection.commit();
      } catch (IOException | SQLException | RuntimeException exception) {
        connection.rollback();
        throw exception;
      }
    }
  }

  private static void grantRuntimePrivileges(final Connection connection, final String role)
      throws SQLException {
    if (!SQL_IDENTIFIER.matcher(role).matches()) {
      throw new IllegalArgumentException("runtimeDatabaseUser must be a simple SQL identifier");
    }
    String quotedRole = '"' + role + '"';
    try (Statement statement = connection.createStatement()) {
      statement.execute("REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM " + quotedRole);
      statement.execute(
          "GRANT SELECT, INSERT ON tracking_sessions, tracking_participants, tracking_events TO "
              + quotedRole);
      statement.execute(
          "GRANT UPDATE (status, ended_at, finish_elapsed_ms, final_sequence, "
              + "aborted_at_puzzle_id) ON tracking_sessions TO "
              + quotedRole);
      statement.execute(
          "GRANT UPDATE (room_played_before) ON tracking_participants TO " + quotedRole);
    }
  }

  private static void createHistory(final Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(
          "CREATE TABLE IF NOT EXISTS tracking_schema_migrations ("
              + "version TEXT PRIMARY KEY, applied_at TIMESTAMPTZ NOT NULL DEFAULT now())");
    }
  }

  private static boolean applied(final Connection connection, final String version)
      throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement("SELECT 1 FROM tracking_schema_migrations WHERE version = ?")) {
      statement.setString(1, version);
      try (ResultSet result = statement.executeQuery()) {
        return result.next();
      }
    }
  }

  private static String readMigration(final String resource) throws IOException {
    try (InputStream input = MigrationRunner.class.getResourceAsStream(resource)) {
      if (input == null) {
        throw new IOException("Missing migration resource " + resource);
      }
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private record Migration(String version, String resource) {}
}
