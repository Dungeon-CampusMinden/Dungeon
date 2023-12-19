package contrib.crafting;

/**
 * Exception which is thrown when a recipe is invalid.
 *
 * <p>It is a runtime exception because it is not expected to happen during normal execution.
 */
public final class InvalidRecipeException extends RuntimeException {
  public InvalidRecipeException(final String message) {
    super(message);
  }
}
