package dungine.exception;

import de.fwatermann.dungine.ecs.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Exception stating that a component is missing.
 *
 * <p>This exception is thrown by (e.g. a {@link de.fwatermann.dungine.ecs.System System}) when a required component is
 * missing on an entity that is being processed.
 */
public final class MissingComponentException extends NullPointerException {

  private static final Logger LOGGER = LogManager.getLogger(MissingComponentException.class);

  /**
   * Constructs a new MissingComponentException with the specified detail message.
   *
   * @param message Detail message. Should contain the name of the missing component.
   */
  public MissingComponentException(final String message) {
    super("Missing Component:" + message);
    LOGGER.error("Missing Component: {}", message);
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
