package core.gui.util;

import core.gui.backend.opengl.OpenGLUtil;
import core.utils.logging.CustomLogLevel;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {
    private static final Logger LOGGER = Logger.getLogger(OpenGLUtil.class.getName());

    /**
     * Log a message to the console and the logger.
     *
     * @param level Log level.
     * @param format Message format.
     * @param args Message arguments.
     */
    public static void log(Level level, String format, Object... args) {
        if (!format.endsWith("\n") && !format.endsWith("%n")) format += "\n";
        if (level == CustomLogLevel.ERROR
                || level == CustomLogLevel.SEVERE
                || level == Level.WARNING) {
            System.err.printf("[" + level.getName() + "] " + format, args);
        } else {
            System.out.printf("[" + level.getName() + "] " + format, args);
        }
        LOGGER.log(level, String.format(format, args));
    }

    /**
     * Log a message to the console and the logger.
     *
     * @param level Log level.
     * @param format Message format.
     * @param throwable Throwable to log.
     * @param args Message arguments.
     */
    public static void log(Level level, String format, Throwable throwable, Object... args) {
        if (!format.endsWith("\n") && !format.endsWith("%n")) format += "\n";
        format = "[" + level.getName() + "] " + format;
        if (level == CustomLogLevel.ERROR
                || level == CustomLogLevel.SEVERE
                || level == Level.WARNING) {
            System.err.printf("[" + level.getName() + "] " + format, args);
            throwable.printStackTrace(System.err);
        } else {
            System.out.printf("[" + level.getName() + "] " + format, args);
            throwable.printStackTrace(System.out);
        }
        LOGGER.log(level, String.format(format, args), throwable);
    }
}
