package logging;

import java.util.logging.Level;

/** This class provides custom log levels to provide further log separation possibilities. */
public class CustomLogLevel extends Level {
    public static CustomLogLevel FATAL = new CustomLogLevel("FATAL", 1100);
    public static CustomLogLevel ERROR = new CustomLogLevel("ERROR", 950);
    public static CustomLogLevel DEBUG = new CustomLogLevel("DEBUG", 200);
    public static CustomLogLevel TRACE = new CustomLogLevel("TRACE", 100);

    protected CustomLogLevel(String name, int value) {
        super(name, value);
    }
}
