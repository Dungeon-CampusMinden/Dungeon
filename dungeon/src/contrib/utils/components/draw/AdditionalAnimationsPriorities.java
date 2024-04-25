package contrib.utils.components.draw;

/**
 * Collection of animations for entities which can attack or get attacked.
 *
 * @see core.utils.components.draw.CoreAnimationPriorities
 */
public enum AdditionalAnimationsPriorities {
  /** WTF? . */
  DIE(5000),
  /** WTF? . */
  HIT(4000),
  /** WTF? . */
  FIGHT(3000);

  private final int priority;

  AdditionalAnimationsPriorities(int priority) {
    this.priority = priority;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public int priority() {
    return priority;
  }
}
