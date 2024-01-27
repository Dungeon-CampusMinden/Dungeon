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
  private final String path;

  /**
   * Create a new Path.
   *
   * @param path Path to store, as a string.
   */
  public SimpleIPath(String path) {
    this.path = path;
  }

  @Override
  public String pathString() {
    return path;
  }

  @Override
  public int priority() {
    return 0;
  }

  @Override
  public String toString() {
    return String.format("SimpleIPath{path=%s}", this.path);
  }
}
