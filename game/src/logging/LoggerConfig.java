package logging;

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

public class LoggerConfig {
    private static Logger baseLogger;
    private static FileHandler customFileHandler;

    private static void createCustomFileHandler() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy'T'HH-mm-ss");
        String timestamp = dateFormat.format(new Date());
        String directoryPath = "./logs/systemlogs/";
        String filepath = directoryPath + timestamp + ".log";
        File newLogFile = new File(filepath);
        try {
            Files.createDirectories(Paths.get(directoryPath));
            if (newLogFile.exists()) {
                baseLogger.info("Logfile already exists;");
            } else {
                newLogFile.createNewFile();
                baseLogger.info("Logfile '" + filepath + "' was created.");
            }
            customFileHandler = new FileHandler(filepath);
            customFileHandler.setFormatter(new SimpleFormatter());
        } catch (IOException ioE) {
            baseLogger.warning(
                    "Creation of FileHandler in class 'LoggerConfig' failed: " + ioE.getMessage());
        }
    }

    /** Creates a new base logger that records all occurring logs to a file. */
    public static void initBaseLogger() {
        baseLogger = Logger.getLogger("");
        baseLogger.setLevel(Level.ALL);
        createCustomFileHandler();

        baseLogger.addHandler(customFileHandler);
    }
}
