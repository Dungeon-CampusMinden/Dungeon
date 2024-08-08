package level.utils;

/** Exception thrown when a level is not found at the specified path. */
public class MissingLevelException extends RuntimeException {

  /**
   * Constructs a new MissingLevelException with the specified path.
   *
   * @param path The path where the level was not found.
   */
  public MissingLevelException(String path) {
    super("No Level found at path: " + path);
  }
}
