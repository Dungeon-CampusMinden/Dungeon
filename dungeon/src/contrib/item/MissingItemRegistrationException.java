package contrib.item;

/** Exception thrown when a required item ID has not been registered. */
public final class MissingItemRegistrationException extends RuntimeException {
  /**
   * Creates a new exception with the provided message.
   *
   * @param message the exception message
   */
  public MissingItemRegistrationException(String message) {
    super(message);
  }
}
