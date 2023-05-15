package core.utils.components;

import core.utils.logging.CustomLogLevel;

import java.util.logging.Logger;

public class MissingComponentException extends NullPointerException {

    public MissingComponentException(String message) {
        super("Missing Component:" + message);
        Logger exceptionLogger = Logger.getLogger(this.getClass().getName());
        exceptionLogger.log(CustomLogLevel.FATAL, "Missing Component: " + message);
    }
}
