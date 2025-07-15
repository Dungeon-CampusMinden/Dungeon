package core.utils.components.draw;

/**
 * Priorities for the core animations.
 *
 * <p>Priorities define the order in which animations will be drawn by the {@link
 * core.systems.DrawSystem}
 *
 * @see CoreAnimations
 */
public enum CoreAnimationPriorities {
  /** WTF? . */
  IDLE(1000),
  /** WTF? . */
  RUN(2000),
  /** WTF? . */
  DEFAULT(0);

  private final int priority;

  /**
   * Create an enum-value with the specified priority.
   *
   * @param priority The priority value for the animation.
   */
  CoreAnimationPriorities(int priority) {
    this.priority = priority;
  }

  /**
   * Gets the priority value.
   *
   * @return The priority value.
   */
  public int priority() {
    return priority;
  }
}
