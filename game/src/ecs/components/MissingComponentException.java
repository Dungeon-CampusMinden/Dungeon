package ecs.components;

import java.util.logging.Logger;
import logging.CustomLogLevel;

public class MissingComponentException extends NullPointerException {

    public MissingComponentException(String message) {
        super("Missing Component:" + message);
        Logger exceptionLogger = Logger.getLogger(this.getClass().getName());
        exceptionLogger.log(CustomLogLevel.FATAL, "Missing Component: " + message);
    }
}
