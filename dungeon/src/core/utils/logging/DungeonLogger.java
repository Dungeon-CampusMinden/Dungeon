package core.utils.logging;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A simple logging facade for the Dungeon project.
 *
 * <p>Provides a clean API with support for formatted messages using {@code {}} placeholders.
 *
 * <p>Usage:
 *
 * <pre>
 * private static final DungeonLogger LOGGER = DungeonLogger.getLogger(MyClass.class);
 *
 * LOGGER.info("Starting process");
 * LOGGER.debug("Processing value: {}", value);
 * LOGGER.error("Failed to load: {}", filename, exception);
 * </pre>
 */
public final class DungeonLogger {
  private final Logger baseLogger;
  private final String className;

  private DungeonLogger(Logger baseLogger, String className) {
    this.baseLogger = baseLogger;
    this.className = className;
  }

  /**
   * Get a logger for the specified class.
   *
   * @param clazz The class to create a logger for.
   * @return A DungeonLogger instance.
   */
  public static DungeonLogger getLogger(Class<?> clazz) {
    return new DungeonLogger(Logger.getLogger(clazz.getName()), clazz.getSimpleName());
  }

  /**
   * Get a logger with the specified name.
   *
   * @param name The logger name.
   * @return A DungeonLogger instance.
   */
  public static DungeonLogger getLogger(String name) {
    return new DungeonLogger(Logger.getLogger(name), name);
  }

  // ===== TRACE =====

  /**
   * Log a message at TRACE level.
   *
   * @param msg The message.
   */
  public void trace(String msg) {
    log(DungeonLogLevel.TRACE, msg);
  }

  /**
   * Log a formatted message at TRACE level.
   *
   * @param format The message format with {} placeholders.
   * @param arg The argument to substitute.
   */
  public void trace(String format, Object arg) {
    log(DungeonLogLevel.TRACE, format, arg);
  }

  /**
   * Log a formatted message at TRACE level.
   *
   * @param format The message format with {} placeholders.
   * @param arg1 The first argument.
   * @param arg2 The second argument.
   */
  public void trace(String format, Object arg1, Object arg2) {
    log(DungeonLogLevel.TRACE, format, arg1, arg2);
  }

  /**
   * Log a formatted message at TRACE level.
   *
   * @param format The message format with {} placeholders.
   * @param args The arguments to substitute.
   */
  public void trace(String format, Object... args) {
    log(DungeonLogLevel.TRACE, format, args);
  }

  // ===== DEBUG =====

  /**
   * Log a message at DEBUG level.
   *
   * @param msg The message.
   */
  public void debug(String msg) {
    log(DungeonLogLevel.DEBUG, msg);
  }

  /**
   * Log a formatted message at DEBUG level.
   *
   * @param format The message format with {} placeholders.
   * @param arg The argument to substitute.
   */
  public void debug(String format, Object arg) {
    log(DungeonLogLevel.DEBUG, format, arg);
  }

  /**
   * Log a formatted message at DEBUG level.
   *
   * @param format The message format with {} placeholders.
   * @param arg1 The first argument.
   * @param arg2 The second argument.
   */
  public void debug(String format, Object arg1, Object arg2) {
    log(DungeonLogLevel.DEBUG, format, arg1, arg2);
  }

  /**
   * Log a formatted message at DEBUG level.
   *
   * @param format The message format with {} placeholders.
   * @param args The arguments to substitute.
   */
  public void debug(String format, Object... args) {
    log(DungeonLogLevel.DEBUG, format, args);
  }

  // ===== INFO =====

  /**
   * Log a message at INFO level.
   *
   * @param msg The message.
   */
  public void info(String msg) {
    log(DungeonLogLevel.INFO, msg);
  }

  /**
   * Log a formatted message at INFO level.
   *
   * @param format The message format with {} placeholders.
   * @param arg The argument to substitute.
   */
  public void info(String format, Object arg) {
    log(DungeonLogLevel.INFO, format, arg);
  }

  /**
   * Log a formatted message at INFO level.
   *
   * @param format The message format with {} placeholders.
   * @param arg1 The first argument.
   * @param arg2 The second argument.
   */
  public void info(String format, Object arg1, Object arg2) {
    log(DungeonLogLevel.INFO, format, arg1, arg2);
  }

  /**
   * Log a formatted message at INFO level.
   *
   * @param format The message format with {} placeholders.
   * @param args The arguments to substitute.
   */
  public void info(String format, Object... args) {
    log(DungeonLogLevel.INFO, format, args);
  }

  // ===== WARN =====

  /**
   * Log a message at WARN level.
   *
   * @param msg The message.
   */
  public void warn(String msg) {
    log(DungeonLogLevel.WARN, msg);
  }

  /**
   * Log a formatted message at WARN level.
   *
   * @param format The message format with {} placeholders.
   * @param arg The argument to substitute.
   */
  public void warn(String format, Object arg) {
    log(DungeonLogLevel.WARN, format, arg);
  }

  /**
   * Log a formatted message at WARN level.
   *
   * @param format The message format with {} placeholders.
   * @param arg1 The first argument.
   * @param arg2 The second argument.
   */
  public void warn(String format, Object arg1, Object arg2) {
    log(DungeonLogLevel.WARN, format, arg1, arg2);
  }

  /**
   * Log a formatted message at WARN level.
   *
   * @param format The message format with {} placeholders.
   * @param args The arguments to substitute.
   */
  public void warn(String format, Object... args) {
    log(DungeonLogLevel.WARN, format, args);
  }

  // ===== ERROR =====

  /**
   * Log a message at ERROR level.
   *
   * @param msg The message.
   */
  public void error(String msg) {
    log(DungeonLogLevel.ERROR, msg);
  }

  /**
   * Log a message at ERROR level with an exception.
   *
   * @param msg The message.
   * @param throwable The exception to log.
   */
  public void error(String msg, Throwable throwable) {
    log(DungeonLogLevel.ERROR, msg, throwable);
  }

  /**
   * Log a formatted message at ERROR level.
   *
   * @param format The message format with {} placeholders.
   * @param arg The argument to substitute.
   */
  public void error(String format, Object arg) {
    log(DungeonLogLevel.ERROR, format, arg);
  }

  /**
   * Log a formatted message at ERROR level.
   *
   * @param format The message format with {} placeholders.
   * @param arg1 The first argument.
   * @param arg2 The second argument (can be Throwable).
   */
  public void error(String format, Object arg1, Object arg2) {
    log(DungeonLogLevel.ERROR, format, arg1, arg2);
  }

  /**
   * Log a formatted message at ERROR level.
   *
   * @param format The message format with {} placeholders.
   * @param args The arguments to substitute (last can be Throwable).
   */
  public void error(String format, Object... args) {
    log(DungeonLogLevel.ERROR, format, args);
  }

  // ===== FATAL =====

  /**
   * Log a message at FATAL level.
   *
   * @param msg The message.
   */
  public void fatal(String msg) {
    log(DungeonLogLevel.FATAL, msg);
  }

  /**
   * Log a message at FATAL level with an exception.
   *
   * @param msg The message.
   * @param throwable The exception to log.
   */
  public void fatal(String msg, Throwable throwable) {
    log(DungeonLogLevel.FATAL, msg, throwable);
  }

  /**
   * Log a formatted message at FATAL level.
   *
   * @param format The message format with {} placeholders.
   * @param arg The argument to substitute.
   */
  public void fatal(String format, Object arg) {
    log(DungeonLogLevel.FATAL, format, arg);
  }

  /**
   * Log a formatted message at FATAL level.
   *
   * @param format The message format with {} placeholders.
   * @param arg1 The first argument.
   * @param arg2 The second argument (can be Throwable).
   */
  public void fatal(String format, Object arg1, Object arg2) {
    log(DungeonLogLevel.FATAL, format, arg1, arg2);
  }

  /**
   * Log a formatted message at FATAL level.
   *
   * @param format The message format with {} placeholders.
   * @param args The arguments to substitute (last can be Throwable).
   */
  public void fatal(String format, Object... args) {
    log(DungeonLogLevel.FATAL, format, args);
  }

  // ===== Internal logging methods =====

  private void log(DungeonLogLevel level, String msg) {
    if (!baseLogger.isLoggable(toJulLevel(level))) {
      return;
    }
    LogRecord record = new LogRecord(toJulLevel(level), msg);
    record.setLoggerName(baseLogger.getName());
    record.setSourceClassName(className);
    baseLogger.log(record);
  }

  private void log(DungeonLogLevel level, String msg, Throwable throwable) {
    if (!baseLogger.isLoggable(toJulLevel(level))) {
      return;
    }
    LogRecord record = new LogRecord(toJulLevel(level), msg);
    record.setLoggerName(baseLogger.getName());
    record.setSourceClassName(className);
    record.setThrown(throwable);
    baseLogger.log(record);
  }

  private void log(DungeonLogLevel level, String format, Object arg) {
    if (!baseLogger.isLoggable(toJulLevel(level))) {
      return;
    }
    String msg = formatMessage(format, arg);
    log(level, msg);
  }

  private void log(DungeonLogLevel level, String format, Object arg1, Object arg2) {
    if (!baseLogger.isLoggable(toJulLevel(level))) {
      return;
    }

    // Check if last argument is a throwable
    if (arg2 instanceof Throwable) {
      String msg = formatMessage(format, arg1);
      log(level, msg, (Throwable) arg2);
    } else {
      String msg = formatMessage(format, arg1, arg2);
      log(level, msg);
    }
  }

  private void log(DungeonLogLevel level, String format, Object... args) {
    if (!baseLogger.isLoggable(toJulLevel(level))) {
      return;
    }

    // Check if last argument is a throwable
    if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
      Object[] msgArgs = new Object[args.length - 1];
      System.arraycopy(args, 0, msgArgs, 0, args.length - 1);
      String msg = formatMessage(format, msgArgs);
      log(level, msg, (Throwable) args[args.length - 1]);
    } else {
      String msg = formatMessage(format, args);
      log(level, msg);
    }
  }

  /**
   * Format a message by replacing {} placeholders with arguments.
   *
   * @param format The format string.
   * @param args The arguments.
   * @return The formatted message.
   */
  private String formatMessage(String format, Object... args) {
    if (format == null) {
      return null;
    }
    if (args == null || args.length == 0) {
      return format;
    }

    StringBuilder result = new StringBuilder(format.length() + 50);
    int argIndex = 0;
    int start = 0;

    while (start < format.length()) {
      int pos = format.indexOf("{}", start);
      if (pos == -1) {
        result.append(format.substring(start));
        break;
      }

      result.append(format, start, pos);
      if (argIndex < args.length) {
        result.append(args[argIndex++]);
      } else {
        result.append("{}");
      }
      start = pos + 2;
    }

    return result.toString();
  }

  /**
   * Convert DungeonLogLevel to java.util.logging.Level.
   *
   * @param level The DungeonLogLevel.
   * @return The corresponding java.util.logging.Level.
   */
  private Level toJulLevel(DungeonLogLevel level) {
    return switch (level) {
      case TRACE -> CustomLogLevel.TRACE;
      case DEBUG -> CustomLogLevel.DEBUG;
      case INFO -> Level.INFO;
      case WARN -> Level.WARNING;
      case ERROR -> CustomLogLevel.ERROR;
      case FATAL -> CustomLogLevel.FATAL;
    };
  }

  /**
   * Add a FileHandler to the logger.
   *
   * @param fileHandler The FileHandler to add.
   */
  public void addHandler(FileHandler fileHandler) {
    baseLogger.addHandler(fileHandler);
  }
}
