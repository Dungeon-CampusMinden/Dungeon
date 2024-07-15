package core.utils.components.draw;

import core.utils.components.path.IPath;

/**
 * Default paths to the animations, and their priority, used by the systems inside the core package.
 *
 * <p>The enums represent a path (as a string) where animations can be found. The path starts at a
 * specific subfolder (e.g., "character/hero"), so the enums have the value of the directory inside
 * this directory (e.g., "character/hero/idle_left").
 *
 * <p>You can use this values for {@link core.components.DrawComponent#queueAnimation(IPath...)}.
 */
public enum CoreAnimations implements IPath {
  /** WTF? . */
  IDLE("idle", CoreAnimationPriorities.IDLE.priority()),
  /** WTF? . */
  IDLE_LEFT("idle_left", CoreAnimationPriorities.IDLE.priority()),
  /** WTF? . */
  IDLE_RIGHT("idle_right", CoreAnimationPriorities.IDLE.priority()),
  /** WTF? . */
  IDLE_UP("idle_up", CoreAnimationPriorities.IDLE.priority()),
  /** WTF? . */
  IDLE_DOWN("idle_down", CoreAnimationPriorities.IDLE.priority()),
  /** WTF? . */
  RUN("run", CoreAnimationPriorities.RUN.priority()),
  /** WTF? . */
  RUN_LEFT("run_left", CoreAnimationPriorities.RUN.priority()),
  /** WTF? . */
  RUN_RIGHT("run_right", CoreAnimationPriorities.RUN.priority()),
  /** WTF? . */
  RUN_UP("run_up", CoreAnimationPriorities.RUN.priority()),
  /** WTF? . */
  RUN_DOWN("run_down", CoreAnimationPriorities.RUN.priority());

  private final String value;
  private final int priority;

  CoreAnimations(final String value, int priority) {
    this.value = value;
    this.priority = priority;
  }

  @Override
  public String pathString() {
    return value;
  }

  @Override
  public String toString() {
    return "CoreAnimation[" + this.value + "]";
  }

  @Override
  public int priority() {
    return priority;
  }
}
