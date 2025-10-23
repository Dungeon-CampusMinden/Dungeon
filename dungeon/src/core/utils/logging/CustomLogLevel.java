package core.utils.logging;

import java.util.logging.Level;

/**
 * Custom log levels that extend {@link java.util.logging.Level} with additional severity levels.
 *
 * <p>These levels fill the gaps in the standard Java logging levels and are used internally by
 * {@link DungeonLogger} to provide a complete set of log levels (TRACE, DEBUG, INFO, WARN, ERROR,
 * FATAL).
 *
 * <p>Standard java.util.logging levels: SEVERE(1000), WARNING(900), INFO(800), CONFIG(700),
 * FINE(500), FINER(400), FINEST(300)
 *
 * <p>Our custom levels: FATAL(1100), ERROR(950), DEBUG(200), TRACE(100)
 *
 * <p><b>Note:</b> For new code, prefer using {@link DungeonLogger} instead of using these levels
 * directly.
 */
public class CustomLogLevel extends Level {
  /**
   * FATAL level (1100) - Critical errors that may cause application termination.
   *
   * <p>Higher severity than SEVERE(1000). Use for unrecoverable errors.
   */
  public static final CustomLogLevel FATAL = new CustomLogLevel("FATAL", 1100);

  /**
   * ERROR level (950) - Error conditions that allow the application to continue.
   *
   * <p>Between SEVERE(1000) and WARNING(900). Use for recoverable errors.
   */
  public static final CustomLogLevel ERROR = new CustomLogLevel("ERROR", 950);

  /**
   * DEBUG level (200) - Detailed debugging information.
   *
   * <p>Between FINEST(300) and our TRACE(100). Use for development debugging.
   */
  public static final CustomLogLevel DEBUG = new CustomLogLevel("DEBUG", 200);

  /**
   * TRACE level (100) - Highly detailed diagnostic information.
   *
   * <p>Below FINEST(300). Use for tracing program execution flow.
   */
  public static final CustomLogLevel TRACE = new CustomLogLevel("TRACE", 100);

  /**
   * Protected constructor for creating custom log levels.
   *
   * @param name The name of the log level.
   * @param value The numeric value of the log level (higher = more severe).
   */
  protected CustomLogLevel(String name, int value) {
    super(name, value);
  }
}
