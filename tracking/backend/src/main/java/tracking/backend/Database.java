package tracking.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class Database {
  private final BackendConfig config;

  Database(final BackendConfig config) {
    this.config = config;
  }

  Connection connect() throws SQLException {
    if (config.databaseUser().isEmpty()) {
      return DriverManager.getConnection(config.databaseUrl());
    }
    return DriverManager.getConnection(
        config.databaseUrl(), config.databaseUser(), config.databasePassword());
  }
}
