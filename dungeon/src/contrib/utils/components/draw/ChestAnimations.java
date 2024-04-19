package contrib.utils.components.draw;

import core.utils.components.draw.CoreAnimationPriorities;
import core.utils.components.path.IPath;

/** A collection of Animations which are special for a chest and not used by any other Entities. */
public enum ChestAnimations implements IPath {
  /** WTF? . */
  CLOSED("idle_closed", CoreAnimationPriorities.IDLE.priority()),
  /** once the chest is open, there are two states: with items or without. */
  OPEN_EMPTY("open_empty", CoreAnimationPriorities.IDLE.priority() + 100),
  /** WTF? . */
  OPEN_FULL("open_full", CoreAnimationPriorities.IDLE.priority() + 100),
  /** animation?! WTF? . */
  OPENING("opening", CoreAnimationPriorities.IDLE.priority() + 200);

  private final String value;
  private final int priority;

  ChestAnimations(final String value, int priority) {
    this.value = value;
    this.priority = priority;
  }

  @Override
  public String pathString() {
    return value;
  }

  @Override
  public String toString() {
    return "ChestAnimation[" + this.value + "]";
  }

  @Override
  public int priority() {
    return priority;
  }
}
