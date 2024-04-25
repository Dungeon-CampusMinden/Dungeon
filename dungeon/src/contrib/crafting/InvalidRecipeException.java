package contrib.crafting;

/**
 * Exception which is thrown when a recipe is invalid.
 *
 * <p>It is a runtime exception because it is not expected to happen during normal execution.
 */
public final class InvalidRecipeException extends RuntimeException {
  /**
   * Calls {@link RuntimeException#RuntimeException(String)} with the given message.
   *
   * @param message the message of the exception
   */
  public InvalidRecipeException(final String message) {
    super(message);
  }
}
