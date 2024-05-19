package core.utils.components.path;

/**
 * Datatype that is used for all path apis in the dungeon framework.
 *
 * @see core.components.DrawComponent
 */
public interface IPath {
  /**
   * Make sure that your enum values are strings so the {@link core.components.DrawComponent} can
   * use them to read in directories.
   *
   * <p>Return the value of the enums.
   *
   * @return The value as a string that can be used as a path
   */
  String pathString();

  /**
   * WTF? .
   *
   * @return foo
   */
  int priority();
}
