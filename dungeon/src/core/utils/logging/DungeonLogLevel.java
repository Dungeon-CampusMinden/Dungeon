package core.utils.logging;

import java.util.logging.Level;

/**
 * Log levels for the Dungeon logging system.
 *
 * <p>Ordered from most verbose (TRACE) to least verbose (FATAL).
 */
public enum DungeonLogLevel {
  /** All log messages. */
  ALL(Level.ALL),

  /** Highly detailed diagnostic information for tracing program execution. */
  TRACE(CustomLogLevel.TRACE),

  /** Detailed information useful for debugging during development. */
  DEBUG(CustomLogLevel.DEBUG),

  /** General informational messages about application progress. */
  INFO(Level.INFO),

  /** Warning messages indicating potential issues. */
  WARN(Level.WARNING),

  /** Error messages indicating failures that allow the application to continue. */
  ERROR(CustomLogLevel.ERROR),

  /** Critical errors that may cause the application to terminate. */
  FATAL(CustomLogLevel.FATAL);

  private final Level julLevel;

  DungeonLogLevel(Level julLevel) {
    this.julLevel = julLevel;
  }

  /**
   * Get the numeric value of this log level.
   *
   * @return The numeric severity value.
   */
  public int value() {
    return julLevel.intValue();
  }

  /**
   * Check if this level is enabled given a configured minimum level.
   *
   * @param configuredLevel The minimum level that should be logged.
   * @return True if this level should be logged.
   */
  public boolean isEnabled(DungeonLogLevel configuredLevel) {
    return this.value() >= configuredLevel.value();
  }

  /**
   * Convert this DungeonLogLevel to the corresponding java.util.logging.Level.
   *
   * @return The corresponding java.util.logging.Level.
   */
  public Level toJulLevel() {
    return julLevel;
  }

  /**
   * Custom log levels that extend {@link java.util.logging.Level} with additional severity levels.
   */
  private static class CustomLogLevel extends Level {
    private static final Level FATAL = new CustomLogLevel("FATAL", 1100);

    private static final Level ERROR = new CustomLogLevel("ERROR", 950);

    private static final Level DEBUG = new CustomLogLevel("DEBUG", 200);

    private static final Level TRACE = new CustomLogLevel("TRACE", 100);

    private CustomLogLevel(String name, int value) {
      super(name, value);
    }
  }
}
