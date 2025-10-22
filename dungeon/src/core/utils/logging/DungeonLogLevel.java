package core.utils.logging;

/**
 * Log levels for the Dungeon logging system.
 *
 * <p>Ordered from most verbose (TRACE) to least verbose (FATAL).
 */
public enum DungeonLogLevel {
  /** Highly detailed diagnostic information for tracing program execution. */
  TRACE(100),

  /** Detailed information useful for debugging during development. */
  DEBUG(200),

  /** General informational messages about application progress. */
  INFO(400),

  /** Warning messages indicating potential issues. */
  WARN(500),

  /** Error messages indicating failures that allow the application to continue. */
  ERROR(950),

  /** Critical errors that may cause the application to terminate. */
  FATAL(1100);

  private final int value;

  DungeonLogLevel(int value) {
    this.value = value;
  }

  /**
   * Get the numeric value of this log level.
   *
   * @return The numeric severity value.
   */
  public int value() {
    return value;
  }

  /**
   * Check if this level is enabled given a configured minimum level.
   *
   * @param configuredLevel The minimum level that should be logged.
   * @return True if this level should be logged.
   */
  public boolean isEnabled(DungeonLogLevel configuredLevel) {
    return this.value >= configuredLevel.value;
  }
}
