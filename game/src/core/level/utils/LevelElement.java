package core.level.utils;

/** Each type of field in a level can be represented by an integer value. */
public enum LevelElement {
  /** This field is a blank */
  SKIP(false),
  /** This field is a floor-field */
  FLOOR(true),
  /** This field is a wall-field */
  WALL(false),
  /** This field is a hole-field */
  HOLE(false),
  /** This field is the exit-field to the next level */
  EXIT(true),

  DOOR(true);

  private final boolean value;

  /**
   * Represents a level element with accessibility information.
   *
   * @param value The accessibility value of the element.
   */
  LevelElement(boolean value) {
    this.value = value;
  }

  /**
   * Checks if the element is accessible.
   *
   * @return true if the element is accessible, code false if not.
   */
  public boolean value() {
    return value;
  }
}
