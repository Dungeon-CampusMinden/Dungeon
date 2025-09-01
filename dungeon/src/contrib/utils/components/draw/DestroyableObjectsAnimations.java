package contrib.utils.components.draw;

import core.utils.components.draw.CoreAnimationPriorities;
import core.utils.components.path.IPath;

/** Enum representing the animations for any destroyable object. */
public enum DestroyableObjectsAnimations implements IPath {
  /** The basic texture of a destroyable object. */
  INTACT("intact", CoreAnimationPriorities.IDLE.priority()),
  /**
   * The animation played while the object breaks apart. Usually includes the final broken texture
   * as the last frame.
   */
  BREAKING("breaking", CoreAnimationPriorities.IDLE.priority() + 200),
  /** The object in its broken state, after the breaking animation. */
  BROKEN("broken", CoreAnimationPriorities.IDLE.priority() + 100);

  private final String value;
  private final int priority;

  DestroyableObjectsAnimations(final String value, int priority) {
    this.value = value;
    this.priority = priority;
  }

  @Override
  public String pathString() {
    return value;
  }

  @Override
  public String toString() {
    return "DestroyableObjectsAnimation[" + this.value + "]";
  }

  @Override
  public int priority() {
    return priority;
  }
}
