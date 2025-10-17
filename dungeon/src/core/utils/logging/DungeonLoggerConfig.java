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
 * levels, and custom formatting.
 *
 * <p>Usage:
 *
 * <pre>
 * DungeonLoggerConfig.builder()
 *     .level(DungeonLogLevel.DEBUG)
 *     .enableConsole(true)
 *     .enableFile(true)
 *     .logDirectory("logs/")
 *     .build();
 * </pre>
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
   * @return A new builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Initialize the logger with default settings (INFO level, console only).
   *
   * <p>Convenience method equivalent to calling {@code builder().build()}.
   */
  public static void initDefault() {
    builder().build();
  }

  /**
   * Initialize the logger with a specific level (console only).
   *
   * <p>Convenience method for quick setup.
   *
   * @param level The log level to use.
   */
  public static void initWithLevel(DungeonLogLevel level) {
    builder().level(level).enableConsole(true).build();
  }

  /**
   * Initialize the logger using java.util.logging.Level (for backward compatibility).
   *
   * @param level The java.util.logging.Level.
   */
  public static void initBaseLogger(Level level) {
    builder().julLevel(level).enableConsole(false).enableFile(true).build();
  }

  /** Builder for DungeonLoggerConfig. */
  public static class Builder {
    private DungeonLogLevel level = DungeonLogLevel.INFO;
    private Level julLevel = null;
    private boolean enableConsole = true;
    private boolean enableFile = false;
    private String logDirectory = "logs/";
    private String logSubDirectory = "systemlogs/";
    private boolean useTimestampInFilename = true;
    private String dateFormat = "dd-MM-yyyy'T'HH-mm-ss";

    /**
     * Set the log level using DungeonLogLevel.
     *
     * @param level The log level.
     * @return This builder.
     */
    public Builder level(DungeonLogLevel level) {
      this.level = level;
      this.julLevel = null;
      return this;
    }

    /**
     * Set the log level using java.util.logging.Level.
     *
     * @param level The java.util.logging.Level.
     * @return This builder.
     */
    public Builder julLevel(Level level) {
      this.julLevel = level;
      return this;
    }

    /**
     * Enable or disable console output.
     *
     * @param enable True to enable console output.
     * @return This builder.
     */
    public Builder enableConsole(boolean enable) {
      this.enableConsole = enable;
      return this;
    }

    /**
     * Enable or disable file output.
     *
     * @param enable True to enable file output.
     * @return This builder.
     */
    public Builder enableFile(boolean enable) {
      this.enableFile = enable;
      return this;
    }

    /**
     * Set the base log directory.
     *
     * @param directory The directory path.
     * @return This builder.
     */
    public Builder logDirectory(String directory) {
      this.logDirectory = directory;
      return this;
    }

    /**
     * Set the log subdirectory (within the base directory).
     *
     * @param subDirectory The subdirectory path.
     * @return This builder.
     */
    public Builder logSubDirectory(String subDirectory) {
      this.logSubDirectory = subDirectory;
      return this;
    }

    /**
     * Enable or disable timestamp in log filename.
     *
     * @param use True to include timestamp.
     * @return This builder.
     */
    public Builder useTimestampInFilename(boolean use) {
      this.useTimestampInFilename = use;
      return this;
    }

    /**
     * Set the date format for log file names.
     *
     * @param dateFormat The SimpleDateFormat pattern.
     * @return This builder.
     */
    public Builder dateFormat(String dateFormat) {
      this.dateFormat = dateFormat;
      return this;
    }

    /**
     * Build and apply the configuration.
     *
     * <p>This will configure the root logger and all handlers.
     */
    public void build() {
      if (initialized) {
        System.err.println("DungeonLoggerConfig already initialized. Skipping re-initialization.");
        return;
      }

      rootLogger = Logger.getLogger("");
      Level effectiveLevel = julLevel != null ? julLevel : toJulLevel(level);
      rootLogger.setLevel(effectiveLevel);

      // Remove existing handlers
      for (Handler handler : rootLogger.getHandlers()) {
        rootLogger.removeHandler(handler);
      }

      // Add console handler if enabled
      if (enableConsole) {
        consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(effectiveLevel);
        consoleHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(consoleHandler);
      }

      // Add file handler if enabled
      if (enableFile) {
        createFileHandler();
        if (fileHandler != null) {
          rootLogger.addHandler(fileHandler);
        }
      }

      initialized = true;
    }

    private void createFileHandler() {
      String filename;
      if (useTimestampInFilename) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String timestamp = sdf.format(new Date());
        filename = timestamp + ".log";
      } else {
        filename = "dungeon.log";
      }

      String directoryPath = System.getProperty("BASELOGDIR", logDirectory) + logSubDirectory;
      String filepath = directoryPath + filename;
      File logFile = new File(filepath);

      try {
        Files.createDirectories(Paths.get(directoryPath));
        if (logFile.exists()) {
          System.out.println("Using existing log file: " + filepath);
        } else {
          logFile.createNewFile();
          System.out.println("Created log file: " + filepath);
        }

        fileHandler = new FileHandler(filepath, true); // append mode
        fileHandler.setLevel(julLevel != null ? julLevel : toJulLevel(level));
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
  }

  /**
   * Shutdown the logging system and close all handlers.
   *
   * <p>Should be called when the application exits.
   */
  public static void shutdown() {
    if (fileHandler != null) {
      fileHandler.close();
    }
    if (consoleHandler != null) {
      consoleHandler.close();
    }
  }
}
