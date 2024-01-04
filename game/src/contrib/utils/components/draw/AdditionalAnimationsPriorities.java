package contrib.utils.components.draw;

/**
 * Collection of animations for entities which can attack or get attacked.
 *
 * @see core.utils.components.draw.CoreAnimationPriorities
 */
public enum AdditionalAnimationsPriorities {
  DIE(5000),
  HIT(4000),
  FIGHT(3000);

  private final int priority;

  AdditionalAnimationsPriorities(int priority) {
    this.priority = priority;
  }

  public int priority() {
    return priority;
  }
}
