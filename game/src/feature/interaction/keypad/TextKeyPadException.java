package feature.interaction.keypad;

/** Exception thrown when an invalid operation occurs while interacting with a text keypad. */
public class TextKeyPadException extends RuntimeException {

  /**
   * Creates a new text keypad exception with the specified message.
   *
   * @param message the detail message describing the reason for the exception
   */
  public TextKeyPadException(String message) {
    super(message);
  }
}
