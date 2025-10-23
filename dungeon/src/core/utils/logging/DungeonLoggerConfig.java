package core.utils.logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * Builder-based configuration for the Dungeon logging system.
 *
 * <p>Provides a flexible way to configure logging with support for console output, file output, log
 * levels, and custom formatting. The logger uses a builder pattern for configuration and ensures
 * single initialization.
 *
 * <p>Usage:
 *
 * <pre>
 * DungeonLoggerConfig.builder()
 *     .consoleLevel(DungeonLogLevel.INFO)
 *     .fileLevel(DungeonLogLevel.DEBUG)
 *     .build();
 *
 * DungeonLoggerConfig.shutdown();
 * </pre>
 *
 * <p>Default configuration: WARNING level for both console and file, both handlers enabled.
 */
public final class DungeonLoggerConfig {
  private static boolean initialized = false;
  private static Logger rootLogger;
  private static FileHandler fileHandler;
  private static ConsoleHandler consoleHandler;

  private DungeonLoggerConfig() {}

  /**
   * Create a new builder for configuring the logging system.
   *
   * @return A new builder instance with default configuration.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Initialize the logger with default settings (WARNING level for both console and file).
   *
   * <p>Convenience method equivalent to calling {@code builder().build()}.
   */
  public static void initDefault() {
    builder().build();
  }

  /**
   * Initialize the logger with a specific level for both console and file output.
   *
   * <p>Convenience method for quick setup with a single log level.
   *
   * @param level The log level to use for both console and file handlers.
   */
  public static void initWithLevel(DungeonLogLevel level) {
    builder().consoleLevel(level).fileLevel(level).build();
  }

  /**
   * Shutdown the logging system and close all handlers.
   *
   * <p>Should be called when the application exits to ensure proper resource cleanup.
   */
  public static void shutdown() {
    if (fileHandler != null) {
      fileHandler.close();
    }
    if (consoleHandler != null) {
      consoleHandler.close();
    }
  }

  /**
   * Returns whether the logging system has been initialized.
   *
   * @return True if initialized, false otherwise.
   */
  public static boolean isInitialized() {
    return initialized;
  }

  /**
   * Builder for DungeonLoggerConfig.
   *
   * <p>Provides a fluent API for configuring the logging system with support for independent
   * console and file log levels.
   */
  public static class Builder {
    private Level consoleLevel = Level.WARNING;
    private Level fileLevel = Level.WARNING;
    private boolean enableConsole = true;
    private boolean enableFile = true;
    private String logDirectory = "logs/";
    private String logSubDirectory = "systemlogs/";
    private boolean useTimestampInFilename = true;
    private String dateFormat = "dd-MM-yyyy'T'HH-mm-ss";

    /**
     * Set the log level for console output using DungeonLogLevel.
     *
     * @param level The log level for console output.
     * @return This builder instance for method chaining.
     */
    public Builder consoleLevel(DungeonLogLevel level) {
      this.consoleLevel = toJulLevel(level);
      return this;
    }

    /**
     * Set the log level for console output using java.util.logging.Level.
     *
     * @param level The java.util.logging.Level for console output.
     * @return This builder instance for method chaining.
     */
    public Builder consoleLevel(Level level) {
      this.consoleLevel = level;
      return this;
    }

    /**
     * Set the log level for file output using DungeonLogLevel.
     *
     * @param level The log level for file output.
     * @return This builder instance for method chaining.
     */
    public Builder fileLevel(DungeonLogLevel level) {
      this.fileLevel = toJulLevel(level);
      return this;
    }

    /**
     * Set the log level for file output using java.util.logging.Level.
     *
     * @param level The java.util.logging.Level for file output.
     * @return This builder instance for method chaining.
     */
    public Builder fileLevel(Level level) {
      this.fileLevel = level;
      return this;
    }

    /**
     * Enable or disable console output.
     *
     * @param enable True to enable console output, false to disable.
     * @return This builder instance for method chaining.
     */
    public Builder enableConsole(boolean enable) {
      this.enableConsole = enable;
      return this;
    }

    /**
     * Enable or disable file output.
     *
     * @param enable True to enable file output, false to disable.
     * @return This builder instance for method chaining.
     */
    public Builder enableFile(boolean enable) {
      this.enableFile = enable;
      return this;
    }

    /**
     * Set the base log directory.
     *
     * <p>The full log path will be: {@code logDirectory + logSubDirectory + filename}
     *
     * @param directory The base directory path. Defaults to "logs/".
     * @return This builder instance for method chaining.
     */
    public Builder logDirectory(String directory) {
      this.logDirectory = directory;
      return this;
    }

    /**
     * Set the log subdirectory within the base directory.
     *
     * <p>The full log path will be: {@code logDirectory + logSubDirectory + filename}
     *
     * @param subDirectory The subdirectory path. Defaults to "systemlogs/".
     * @return This builder instance for method chaining.
     */
    public Builder logSubDirectory(String subDirectory) {
      this.logSubDirectory = subDirectory;
      return this;
    }

    /**
     * Enable or disable timestamp in log filename.
     *
     * <p>If enabled, the filename will be formatted as: {@code [timestamp].log}. If disabled, the
     * filename will be "dungeon.log".
     *
     * @param use True to include timestamp in filename, false to use static name.
     * @return This builder instance for method chaining.
     */
    public Builder useTimestampInFilename(boolean use) {
      this.useTimestampInFilename = use;
      return this;
    }

    /**
     * Set the date format for log file names.
     *
     * <p>Only used if {@code useTimestampInFilename} is true. Must be a valid SimpleDateFormat
     * pattern.
     *
     * @param dateFormat The SimpleDateFormat pattern. Defaults to "dd-MM-yyyy'T'HH-mm-ss".
     * @return This builder instance for method chaining.
     */
    public Builder dateFormat(String dateFormat) {
      this.dateFormat = dateFormat;
      return this;
    }

    /**
     * Build and apply the logging configuration.
     *
     * <p>Configures the root logger with the specified handlers and levels. The root logger level
     * is set to the minimum of the console and file levels to ensure all messages are processed.
     *
     * <p>This method can only be called once. Subsequent calls are ignored with a warning message.
     */
    public void build() {
      if (initialized) {
        System.err.println("DungeonLoggerConfig already initialized. Skipping re-initialization.");
        return;
      }

      rootLogger = Logger.getLogger("");
      Level minLevel = getMinLevel(consoleLevel, fileLevel);
      rootLogger.setLevel(minLevel);

      for (Handler handler : rootLogger.getHandlers()) {
        rootLogger.removeHandler(handler);
      }

      if (enableConsole) {
        consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(consoleLevel);
        consoleHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(consoleHandler);
      }

      if (enableFile) {
        createFileHandler();
        if (fileHandler != null) {
          rootLogger.addHandler(fileHandler);
        }
      }

      initialized = true;
    }

    private void createFileHandler() {
      String filename =
          useTimestampInFilename
              ? new SimpleDateFormat(dateFormat).format(new Date()) + ".log"
              : "dungeon.log";

      String directoryPath = System.getProperty("BASELOGDIR", logDirectory) + logSubDirectory;
      String filepath = directoryPath + filename;
      File logFile = new File(filepath);

      try {
        Files.createDirectories(Paths.get(directoryPath));
        if (logFile.exists()) {
          System.out.println("Using existing log file: " + filepath);
        } else {
          logFile.createNewFile();
        }

        fileHandler = new FileHandler(filepath, true);
        fileHandler.setLevel(fileLevel);
        fileHandler.setFormatter(new SimpleFormatter());

      } catch (IOException e) {
        System.err.println("Failed to create file handler: " + e.getMessage());
        e.printStackTrace();
      }
    }

    private Level toJulLevel(DungeonLogLevel level) {
      return switch (level) {
        case TRACE -> Level.FINEST;
        case DEBUG -> Level.FINE;
        case INFO -> Level.INFO;
        case WARN -> Level.WARNING;
        case ERROR -> Level.SEVERE;
        case FATAL -> CustomLogLevel.FATAL;
      };
    }

    private Level getMinLevel(Level level1, Level level2) {
      return level1.intValue() < level2.intValue() ? level1 : level2;
    }
  }
}
