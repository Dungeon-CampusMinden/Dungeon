package engine.utils.components.path;

/**
 * Datatype that is used for all path apis in the dungeon framework.
 *
 * @see engine.components.DrawComponent
 */
public interface IPath {
  /**
   * Make sure that your enum values are strings so the {@link engine.components.DrawComponent} can
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
