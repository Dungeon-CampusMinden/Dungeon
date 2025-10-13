package coderunner;

/**
 * Represents movement directions used in the Blockly wrapper layer.
 *
 * <p>This enum serves as an abstraction over the core framework’s {@link core.utils.Direction} to
 * allow using alternative, more intuitive or context-specific direction names within Blockly-based
 * environments.
 *
 * <p>The {@link #toDirection()} method maps each {@code Direction} constant in this enum to its
 * corresponding {@link core.utils.Direction} value used internally by the core framework.
 */
public enum Direction {

  /** Represents a position or movement in front of the current entity. */
  INFRONT,

  /** Represents a position or movement behind the current entity. */
  BEHIND,

  /** Represents a position or movement to the left of the current entity. */
  LEFT,

  /** Represents a position or movement to the right of the current entity. */
  RIGHT,

  /** Represents the current position (no movement). */
  HERE;

  /**
   * Converts this {@code Direction} value into the corresponding {@link core.utils.Direction} used
   * by the underlying framework.
   *
   * @return the mapped {@link core.utils.Direction} constant; returns {@link
   *     core.utils.Direction#NONE} for {@link #HERE}.
   */
  public core.utils.Direction toDirection() {
    switch (this) {
      case INFRONT:
        return core.utils.Direction.UP;
      case BEHIND:
        return core.utils.Direction.DOWN;
      case LEFT:
        return core.utils.Direction.LEFT;
      case RIGHT:
        return core.utils.Direction.RIGHT;
      default:
        return core.utils.Direction.NONE;
    }
  }
}
