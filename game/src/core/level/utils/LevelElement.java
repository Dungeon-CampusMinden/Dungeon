package core.level.utils;

/** Each type of field in a level can be represented by an integer value. */
public enum LevelElement {
  /** This field is a blank */
  SKIP(false, true),
  /** This field is a floor-field */
  FLOOR(true, true),
  /** This field is a wall-field */
  WALL(false, false),
  /** This field is a hole-field */
  HOLE(false, false),
  /** This field is the exit-field to the next level */
  EXIT(true, true),

  DOOR(true, true);

  private final boolean value;
  private final boolean canSeeThrough;

  /**
   * Represents a level element with accessibility information.
   *
   * @param value The accessibility value of the element.
   * @param canSeeThrough The Entity can see through this element.
   */
  LevelElement(boolean value, boolean canSeeThrough) {
    this.value = value;
    this.canSeeThrough = canSeeThrough;
  }

  /**
   * Checks if the element is accessible.
   *
   * @return true if the element is accessible, code false if not.
   */
  public boolean value() {
    return value;
  }

  /**
   * Checks if the element can be seen through.
   *
   * @return true if the element can be seen through, code false if not.
   */
  public boolean canSeeThrough() {
    return canSeeThrough;
  }
}
