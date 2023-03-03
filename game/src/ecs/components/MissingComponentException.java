package ecs.components;

import java.util.logging.Logger;

public class MissingComponentException extends NullPointerException {

    public MissingComponentException(String message) {
        super("Missing Component:" + message);
        Logger exceptionLogger = Logger.getLogger(this.getClass().getName());
        exceptionLogger.severe("Missing Component: " + message);
    }
}
