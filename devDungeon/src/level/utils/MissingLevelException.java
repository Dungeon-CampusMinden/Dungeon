package level.utils;

public class MissingLevelException extends RuntimeException {
  public MissingLevelException(int levelNumber) {
    super("No Levels found with ID: " + levelNumber);
  }

  public MissingLevelException(String path) {
    super("No Level found at path: " + path);
  }
}
