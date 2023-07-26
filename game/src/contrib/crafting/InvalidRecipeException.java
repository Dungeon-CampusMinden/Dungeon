package contrib.crafting;

/**
 * Exception which is thrown when a recipe is invalid.
 *
 * <p>It is a runtime exception because it is not expected to happen during normal execution.
 */
public class InvalidRecipeException extends RuntimeException {
    public InvalidRecipeException(String message) {
        super(message);
    }
}
