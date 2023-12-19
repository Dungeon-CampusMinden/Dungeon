package core.utils.components;

import core.Entity;
import core.utils.logging.CustomLogLevel;
import java.util.logging.Logger;

/**
 * Exception stating that a component is missing.
 *
 * <p>This exception is thrown by (e.g. a {@link core.System System}) when a required component is
 * missing on an entity that is being processed.
 */
public final class MissingComponentException extends NullPointerException {

  /**
   * Constructs a new MissingComponentException with the specified detail message.
   *
   * @param message Detail message. Should contain the name of the missing component.
   */
  public MissingComponentException(final String message) {
    super("Missing Component:" + message);
    Logger exceptionLogger = Logger.getLogger(this.getClass().getName());
    exceptionLogger.log(CustomLogLevel.FATAL, "Missing Component: " + message);
  }

  /**
   * Create a new {@link MissingComponentException} that contains the default message, that the
   * given entity is missing the given component.
   *
   * @param entity Entity that is missing a component
   * @param klass Class of the Component that is missing
   * @return the created MissingComponentException
   */
  public static MissingComponentException build(final Entity entity, final Class<?> klass) {
    return new MissingComponentException(entity + " is missing " + klass.getName());
  }
}
