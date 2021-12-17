package level.generator.dungeong.graphg;

/**
 * Thrown when a graph cannot be planar due to the demand requirements.
 *
 * @author Andre Matutat
 */
public class CantBePlanarException extends Exception {
    // Parameterless Constructor
    public CantBePlanarException() {}

    // Constructor that accepts a message
    public CantBePlanarException(String message) {
        super(message);
    }
}
