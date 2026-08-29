package tracking.backend;

/** Applies packaged tracking migrations and exits. */
public final class TrackingMigrate {
  private TrackingMigrate() {}

  /**
   * Connects with the configured migration role and applies pending migrations.
   *
   * @param arguments unused command-line arguments
   */
  public static void main(final String[] arguments) throws Exception {
    BackendConfig config = BackendConfig.load();
    MigrationRunner.migrate(new Database(config), config.runtimeDatabaseUser());
  }
}
