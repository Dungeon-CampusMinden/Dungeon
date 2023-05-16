package core.utils.components;

import core.utils.logging.CustomLogLevel;

import java.util.logging.Logger;

/**
 * Exception stating that a component is missing.
 *
 * <p>This exception is thrown by (e.g. a {@link core.System System}) when a required component is
 * missing on an entity that is being processed.
 */
public class MissingComponentException extends NullPointerException {

    /**
     * Constructs a new MissingComponentException with the specified detail message.
     *
     * @param message Detail message. Should contain the name of the missing component.
     */
    public MissingComponentException(String message) {
        super("Missing Component:" + message);
        Logger exceptionLogger = Logger.getLogger(this.getClass().getName());
        exceptionLogger.log(CustomLogLevel.FATAL, "Missing Component: " + message);
    }
}
