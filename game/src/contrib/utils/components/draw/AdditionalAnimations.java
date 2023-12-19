package contrib.utils.components.draw;

import core.utils.components.draw.CoreAnimations;
import core.utils.components.path.IPath;

/**
 * This enum stores the paths to the animations, and their priority, used by the systems inside the
 * contrib package.
 *
 * <p>Add your own path if you need a new animation-type (like jumping).
 *
 * @see core.components.DrawComponent
 * @see IPath
 * @see CoreAnimations
 */
public enum AdditionalAnimations implements IPath {
  DIE("die", AdditionalAnimationsPriorities.DIE.priority()),
  DIE_LEFT("die_left", AdditionalAnimationsPriorities.DIE.priority()),
  DIE_RIGHT("die_right", AdditionalAnimationsPriorities.DIE.priority()),
  DIE_UP("die_up", AdditionalAnimationsPriorities.DIE.priority()),
  DIE_DOWN("die_down", AdditionalAnimationsPriorities.DIE.priority()),
  HIT("hit", AdditionalAnimationsPriorities.HIT.priority()),
  ATTACK("attack", AdditionalAnimationsPriorities.FIGHT.priority()),
  FIGHT_LEFT("fight_left", AdditionalAnimationsPriorities.FIGHT.priority()),
  FIGHT_RIGHT("fight_right", AdditionalAnimationsPriorities.FIGHT.priority()),
  FIGHT_UP("fight_up", AdditionalAnimationsPriorities.FIGHT.priority()),
  FIGHT_DOWN("fight_down", AdditionalAnimationsPriorities.FIGHT.priority());

  private final String value;
  private final int priority;

  AdditionalAnimations(final String value, int priority) {
    this.value = value;
    this.priority = priority;
  }

  @Override
  public String pathString() {
    return value;
  }

  @Override
  public String toString() {
    return "AdditionalAnimation[" + this.value + "]";
  }

  @Override
  public int priority() {
    return priority;
  }
}
