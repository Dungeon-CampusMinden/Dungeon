package core.utils;

import core.utils.logging.CustomLogLevel;
import core.utils.logging.LoggerConfig;

import java.util.logging.Logger;

/** Exception stating that there is no hero in the game. */
public final class MissingHeroException extends NullPointerException {

    /**
     * Constructs a new MissingHeroException with the specified detail message.
     *
     * @param message Detail message.
     */
    public MissingHeroException(final String message) {
        super("There is no hero: " + message);
        Logger exceptionLogger = LoggerConfig.getLogger(this.getClass().getName());
        exceptionLogger.log(CustomLogLevel.FATAL, "There is no hero: " + message);
    }

    /** Constructs a new MissingHeroException with default message. */
    public MissingHeroException() {
        super("There is no hero!");
        Logger exceptionLogger = LoggerConfig.getLogger(this.getClass().getName());
        exceptionLogger.log(CustomLogLevel.FATAL, "There is no hero!");
    }
}
