package de.fwatermann.dungine.ui.layout;

public class UnitReadOnly extends Unit {

  static final UnitReadOnly auto = new UnitReadOnly(0, UnitType.AUTO);

  public UnitReadOnly(float value, UnitType type) {
    super(value, type);
  }

  public static UnitReadOnly of(int value, UnitType type) {
    return new UnitReadOnly(value, type);
  }

  public static UnitReadOnly auto() {
    return auto;
  }

  public static UnitReadOnly pixel(int value) {
    return new UnitReadOnly(value, UnitType.PIXEL);
  }

  public static UnitReadOnly px(int value) {
    return new UnitReadOnly(value, UnitType.PIXEL);
  }

  public static UnitReadOnly percent(float value) {
    return new UnitReadOnly(value, UnitType.PERCENT);
  }

  public static UnitReadOnly viewportWidth(float value) {
    return new UnitReadOnly(value, UnitType.VIEWPORT_WIDTH);
  }

  public static UnitReadOnly vW(float value) {
    return new UnitReadOnly(value, UnitType.VIEWPORT_WIDTH);
  }

  public static UnitReadOnly viewportHeight(float value) {
    return new UnitReadOnly(value, UnitType.VIEWPORT_HEIGHT);
  }

  public static UnitReadOnly vH(float value) {
    return new UnitReadOnly(value, UnitType.VIEWPORT_HEIGHT);
  }

  @Override
  public Unit value(float value) {
    throw new UnsupportedOperationException("Cannot change value of ReadOnlyUnit");
  }

  @Override
  public Unit type(UnitType type) {
    throw new UnsupportedOperationException("Cannot change type of ReadOnlyUnit");
  }
}
