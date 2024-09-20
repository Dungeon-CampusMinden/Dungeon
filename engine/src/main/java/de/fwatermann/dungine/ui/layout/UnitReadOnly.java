package de.fwatermann.dungine.ui.layout;

/**
 * The `UnitReadOnly` class represents a read-only unit of measurement.
 * It extends the `Unit` class and provides various factory methods to create instances
 * of read-only units with different types such as pixel, percentage, viewport width, and viewport height.
 *
 * <p>This class ensures that the value and type of the unit cannot be changed once it is created.
 * Any attempt to modify the value or type will result in an `UnsupportedOperationException`.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * UnitReadOnly unit = UnitReadOnly.pixel(10);
 * }</pre>
 *
 * @see Unit
 */
public class UnitReadOnly extends Unit {

  static final UnitReadOnly auto = new UnitReadOnly(0, UnitType.AUTO);

  /**
   * Constructs a UnitReadOnly with the specified value and type.
   *
   * @param value the value of the unit.
   * @param type the type of the unit.
   */
  public UnitReadOnly(float value, UnitType type) {
    super(value, type);
  }

  /**
   * Creates a new UnitReadOnly with the specified value and type.
   *
   * @param value the value of the unit.
   * @param type the type of the unit.
   * @return a new UnitReadOnly instance.
   */
  public static UnitReadOnly of(int value, UnitType type) {
    return new UnitReadOnly(value, type);
  }

  /**
   * Returns a UnitReadOnly representing an automatic value.
   *
   * @return a UnitReadOnly with an automatic value.
   */
  public static UnitReadOnly auto() {
    return auto;
  }

  /**
   * Creates a new UnitReadOnly with the specified pixel value.
   *
   * @param value the pixel value of the unit.
   * @return a new UnitReadOnly instance with pixel type.
   */
  public static UnitReadOnly pixel(int value) {
    return new UnitReadOnly(value, UnitType.PIXEL);
  }

  /**
   * Creates a new UnitReadOnly with the specified pixel value.
   *
   * @param value the pixel value of the unit.
   * @return a new UnitReadOnly instance with pixel type.
   */
  public static UnitReadOnly px(int value) {
    return new UnitReadOnly(value, UnitType.PIXEL);
  }

  /**
   * Creates a new UnitReadOnly with the specified percentage value.
   *
   * @param value the percentage value of the unit.
   * @return a new UnitReadOnly instance with percentage type.
   */
  public static UnitReadOnly percent(float value) {
    return new UnitReadOnly(value, UnitType.PERCENT);
  }

  /**
   * Creates a new UnitReadOnly with the specified viewport width value.
   *
   * @param value the viewport width value of the unit.
   * @return a new UnitReadOnly instance with viewport width type.
   */
  public static UnitReadOnly viewportWidth(float value) {
    return new UnitReadOnly(value, UnitType.VIEWPORT_WIDTH);
  }

  /**
   * Creates a new UnitReadOnly with the specified viewport width value.
   *
   * @param value the viewport width value of the unit.
   * @return a new UnitReadOnly instance with viewport width type.
   */
  public static UnitReadOnly vW(float value) {
    return new UnitReadOnly(value, UnitType.VIEWPORT_WIDTH);
  }

  /**
   * Creates a new UnitReadOnly with the specified viewport height value.
   *
   * @param value the viewport height value of the unit.
   * @return a new UnitReadOnly instance with viewport height type.
   */
  public static UnitReadOnly viewportHeight(float value) {
    return new UnitReadOnly(value, UnitType.VIEWPORT_HEIGHT);
  }

  /**
   * Creates a new UnitReadOnly with the specified viewport height value.
   *
   * @param value the viewport height value of the unit.
   * @return a new UnitReadOnly instance with viewport height type.
   */
  public static UnitReadOnly vH(float value) {
    return new UnitReadOnly(value, UnitType.VIEWPORT_HEIGHT);
  }

  /**
   * Throws an UnsupportedOperationException as the value of a ReadOnlyUnit cannot be changed.
   *
   * @param value the new value of the unit.
   * @return nothing, as this operation is unsupported.
   * @throws UnsupportedOperationException always.
   */
  @Override
  public Unit value(float value) {
    throw new UnsupportedOperationException("Cannot change value of ReadOnlyUnit");
  }

  /**
   * Throws an UnsupportedOperationException as the type of a ReadOnlyUnit cannot be changed.
   *
   * @param type the new type of the unit.
   * @return nothing, as this operation is unsupported.
   * @throws UnsupportedOperationException always.
   */
  @Override
  public Unit type(UnitType type) {
    throw new UnsupportedOperationException("Cannot change type of ReadOnlyUnit");
  }
}
