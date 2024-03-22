package core.utils.logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configuration for the Logger in the Dungeon.
 *
 * <p>Call {@link #initBaseLogger} at the start of the program.
 *
 * <p>Will create a new Logfile and write the log messages into it. Disables the output of log
 * messages on the shell.
 */
public final class LoggerConfig {
  private static Logger baseLogger;
  private static FileHandler customFileHandler;

  private static void createCustomFileHandler() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'HH-mm-ss");
    String timestamp = dateFormat.format(new Date());
    String directoryPath = System.getProperty("BASELOGDIR", "logs/") + "systemlogs/";
    String filepath = directoryPath + timestamp + ".log";
    File newLogFile = new File(filepath);
    try {
      Files.createDirectories(Paths.get(directoryPath));
      if (newLogFile.exists()) {
        baseLogger.info("Logfile already exists;");
      } else {
        boolean result = newLogFile.createNewFile();
        if (result) {
          baseLogger.info("Logfile '" + filepath + "' was created.");
        } else {
          baseLogger.warning("Logfile '" + filepath + "' could not be created.");
        }
      }
      customFileHandler = new FileHandler(filepath);
      customFileHandler.setFormatter(new SimpleFormatter());
    } catch (IOException ioE) {
      baseLogger.warning(
          "Creation of FileHandler in class 'LoggerConfig' failed: " + ioE.getMessage());
    }
  }

  /**
   * Initialize the base logger.
   *
   * <p>Set a logging level, and remove the console handler, and write all log messages into the log
   * files.
   *
   * @param level Set logging level to {@code level}
   */
  public static void initBaseLogger(Level level) {
    baseLogger = Logger.getLogger("");
    baseLogger.setLevel(level);

    baseLogger.removeHandler(baseLogger.getHandlers()[0]);

    createCustomFileHandler();
    baseLogger.addHandler(customFileHandler);
  }
}
