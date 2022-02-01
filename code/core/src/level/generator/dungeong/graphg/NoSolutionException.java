package level.generator.dungeong.graphg;

/**
 * Thrown when no solution could be found.
 *
 * @author Andre Matutat
 */
public class NoSolutionException extends Exception {
    // Parameterless Constructor
    public NoSolutionException() {}

    // Constructor that accepts a message
    public NoSolutionException(String message) {
        super(message);
    }
}
