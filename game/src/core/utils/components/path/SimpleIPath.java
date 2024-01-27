package core.utils.components.path;

import com.badlogic.gdx.files.FileHandle;

/**
 * Simple implementation of the {@link IPath} interface.
 *
 * <p>Can be used in the dungeon framework as a Path parameter.
 *
 * <p>This class stores a String. The String will be interpreted as a Path to a file or directory in
 * the dungeon framework.
 */
public class SimpleIPath implements IPath {
  private final FileHandle fileHandle;

  /** Create a new Path. */
  public SimpleIPath(String libgdxFileName) {
    this.fileHandle = new FileHandle(libgdxFileName);
  }

  @Override
  public String pathString() {
    return fileHandle.path();
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
