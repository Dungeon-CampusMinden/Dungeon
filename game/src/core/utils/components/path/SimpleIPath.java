package core.utils.components.path;

/**
 * Simple implementation of the {@link IPath} interface.
 *
 * <p>Can be used in the dungeon framework as a Path parameter.
 *
 * <p>This class stores a String. The String will be interpreted as a Path to a file or directory in
 * the dungeon framework.
 */
public class SimpleIPath implements IPath {
  private final String pathString;
  private final int priority;

  public SimpleIPath(String pathString) {
    this(pathString, 0);
  }

  public SimpleIPath(String pathString, int priority) {
    this.pathString = convertPath(pathString);
    this.priority = priority;
  }

  /**
   * Make sure that your enum values are strings so the {@link core.components.DrawComponent} can
   * use them to read in directories.
   *
   * <p>Return the value of the enums.
   *
   * @return The value as a string that can be used as a path
   */
  @Override
  public String pathString() {
    return pathString;
  }

  @Override
  public int priority() {
    return priority;
  }

  @Override
  public String toString() {
    return String.format("SimpleIPath{path=%s}", this.path);
  }
}
