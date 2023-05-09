package api.components;

import java.util.logging.Logger;
import api.utils.logging.CustomLogLevel;

public class MissingComponentException extends NullPointerException {

    public MissingComponentException(String message) {
        super("Missing Component:" + message);
        Logger exceptionLogger = Logger.getLogger(this.getClass().getName());
        exceptionLogger.log(CustomLogLevel.FATAL, "Missing Component: " + message);
    }
}
