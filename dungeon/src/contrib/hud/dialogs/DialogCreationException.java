package contrib.hud.dialogs;

/** Runtime exception thrown when a dialog cannot be constructed. */
public class DialogCreationException extends RuntimeException {

  /**
   * Create a new DialogCreationException with a message.
   *
   * @param message the message describing the exception
   */
  public DialogCreationException(String message) {
    super(message);
  }

  /**
   * Create a new DialogCreationException with a message and a cause.
   *
   * @param message the message describing the exception
   * @param cause the cause of the exception
   */
  public DialogCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
