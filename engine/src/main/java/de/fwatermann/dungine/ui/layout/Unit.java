package de.fwatermann.dungine.ui.layout;

import org.joml.Vector2i;

/**
 * The `Unit` class represents a unit of measurement with a specific value and type.
 * It provides various factory methods to create instances of units with different types
 * such as pixel, percentage, viewport width, and viewport height.
 *
 * <p>This class allows the value and type of the unit to be set and retrieved.
 * It also provides a method to convert the unit to pixels based on the viewport size
 * and relative value for percentage-based units.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Unit unit = Unit.pixel(10);
 * }</pre>
 *
 * @see UnitReadOnly
 */
public class Unit {

  private float value = 0.0f;
  private UnitType type = UnitType.PIXEL;

  /**
   * Constructs a Unit with the specified value and type.
   *
   * @param value the value of the unit.
   * @param type the type of the unit.
   */
  protected Unit (float value, UnitType type) {
    this.value = value;
    this.type = type;
  }

  /**
   * Creates a new Unit with the specified value and type.
   *
   * @param value the value of the unit.
   * @param type the type of the unit.
   * @return a new Unit instance.
   */
  public static Unit of(int value, UnitType type) {
    return new Unit(value, type);
  }

  /**
   * Returns a Unit representing an automatic value.
   *
   * @return a Unit with an automatic value.
   */
  public static Unit auto() {
    return UnitReadOnly.auto;
  }

  /**
   * Creates a new Unit with the specified pixel value.
   *
   * @param value the pixel value of the unit.
   * @return a new Unit instance with pixel type.
   */
  public static Unit pixel(float value) {
    return new Unit(value, UnitType.PIXEL);
  }

  /**
   * Creates a new Unit with the specified pixel value.
   *
   * @param value the pixel value of the unit.
   * @return a new Unit instance with pixel type.
   */
  public static Unit px(float value) {
    return new Unit(value, UnitType.PIXEL);
  }

  /**
   * Creates a new Unit with the specified percentage value.
   *
   * @param value the percentage value of the unit.
   * @return a new Unit instance with percentage type.
   */
  public static Unit percent(float value) {
    return new Unit(value, UnitType.PERCENT);
  }

  /**
   * Creates a new Unit with the specified viewport width value.
   *
   * @param value the viewport width value of the unit.
   * @return a new Unit instance with viewport width type.
   */
  public static Unit viewportWidth(float value) {
    return new Unit(value, UnitType.VIEWPORT_WIDTH);
  }

  /**
   * Creates a new Unit with the specified viewport width value.
   *
   * @param value the viewport width value of the unit.
   * @return a new Unit instance with viewport width type.
   */
  public static Unit vW(float value) {
    return new Unit(value, UnitType.VIEWPORT_WIDTH);
  }

  /**
   * Creates a new Unit with the specified viewport height value.
   *
   * @param value the viewport height value of the unit.
   * @return a new Unit instance with viewport height type.
   */
  public static Unit viewportHeight(float value) {
    return new Unit(value, UnitType.VIEWPORT_HEIGHT);
  }

  /**
   * Creates a new Unit with the specified viewport height value.
   *
   * @param value the viewport height value of the unit.
   * @return a new Unit instance with viewport height type.
   */
  public static Unit vH(float value) {
    return new Unit(value, UnitType.VIEWPORT_HEIGHT);
  }

  /**
   * Returns the value of the unit.
   *
   * @return the value of the unit.
   */
  public float value() {
    return this.value;
  }

  /**
   * Sets the value of the unit.
   *
   * @param value the new value of the unit.
   * @return the updated Unit instance.
   */
  public Unit value(float value) {
    this.value = value;
    return this;
  }

  /**
   * Returns the type of the unit.
   *
   * @return the type of the unit.
   */
  public UnitType type() {
    return this.type;
  }

  /**
   * Sets the type of the unit.
   *
   * @param type the new type of the unit.
   * @return the updated Unit instance.
   */
  public Unit type(UnitType type) {
    this.type = type;
    return this;
  }

  /**
   * Converts the unit to pixels based on the viewport size and relative value.
   *
   * @param viewportSize the size of the viewport.
   * @param relativeValue the relative value for percentage-based units.
   * @return the pixel value of the unit.
   */
  protected float toPixels(Vector2i viewportSize, float relativeValue) {
    return switch(this.type) {
      case PIXEL -> this.value;
      case PERCENT -> relativeValue * this.value / 100.0f;
      case VIEWPORT_WIDTH -> viewportSize.x * this.value / 100.0f;
      case VIEWPORT_HEIGHT -> viewportSize.y * this.value / 100.0f;
      default -> 0;
    };
  }

  /**
 * Represents the type of a unit of measurement.
 * The `UnitType` enum defines various types of units such as pixel, percentage, viewport width, and viewport height.
 * Each unit type can be either fixed or relative.
 */
public enum UnitType {
  /**
   * Pixel unit type, representing a fixed value in pixels.
   */
  PIXEL(true),

  /**
   * Percentage unit type, representing a relative value as a percentage.
   */
  PERCENT(false),

  /**
   * Viewport width unit type, representing a fixed value as a percentage of the viewport width.
   */
  VIEWPORT_WIDTH(true),

  /**
   * Viewport height unit type, representing a fixed value as a percentage of the viewport height.
   */
  VIEWPORT_HEIGHT(true),

  /**
   * Automatic unit type, representing an automatic value.
   */
  AUTO(false);

  final boolean fixed;

  /**
   * Constructs a UnitType with the specified fixed property.
   *
   * @param fixed whether the unit type is fixed.
   */
  UnitType(boolean fixed) {
    this.fixed = fixed;
  }

  /**
   * Returns whether the unit type is fixed.
   *
   * @return true if the unit type is fixed, false otherwise.
   */
  public boolean isFixed() {
    return this.fixed;
  }
}

  /**
   * Checks if this unit is equal to another object.
   *
   * @param other the object to compare with.
   * @return true if the objects are equal, false otherwise.
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if(other instanceof Unit u) {
      return this.value == u.value && this.type == u.type;
    }
    return false;
  }
}
