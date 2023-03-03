package logging;

import java.util.logging.Level;

public class CustomLogLevel extends Level {
    public static CustomLogLevel FATAL = new CustomLogLevel("FATAL", 1100);
    public static CustomLogLevel ERROR = new CustomLogLevel("ERROR", 950);

    protected CustomLogLevel (String name, int value) {
        super (name, value);
    }

}
