package de.fwatermann.dungine.ui.layout;

import org.joml.Vector2i;

public class Unit {

  private float value = 0.0f;
  private UnitType type = UnitType.PIXEL;

  protected Unit (float value, UnitType type) {
    this.value = value;
    this.type = type;
  }

  public static Unit of(int value, UnitType type) {
    return new Unit(value, type);
  }

  public static Unit auto() {
    return UnitReadOnly.auto;
  }

  public static Unit pixel(int value) {
    return new Unit(value, UnitType.PIXEL);
  }

  public static Unit px(int value) {
    return new Unit(value, UnitType.PIXEL);
  }

  public static Unit percent(float value) {
    return new Unit(value, UnitType.PERCENT);
  }

  public static Unit viewportWidth(float value) {
    return new Unit(value, UnitType.VIEWPORT_WIDTH);
  }

  public static Unit vW(float value) {
    return new Unit(value, UnitType.VIEWPORT_WIDTH);
  }

  public static Unit viewportHeight(float value) {
    return new Unit(value, UnitType.VIEWPORT_HEIGHT);
  }

  public static Unit vH(float value) {
    return new Unit(value, UnitType.VIEWPORT_HEIGHT);
  }

  public float value() {
    return this.value;
  }

  public Unit value(float value) {
    this.value = value;
    return this;
  }

  public UnitType type() {
    return this.type;
  }

  public Unit type(UnitType type) {
    this.type = type;
    return this;
  }

  protected float toPixels(Vector2i viewportSize, float relativeValue) {
    return switch(this.type) {
      case PIXEL -> this.value;
      case PERCENT -> relativeValue * this.value / 100.0f;
      case VIEWPORT_WIDTH -> viewportSize.x * this.value / 100.0f;
      case VIEWPORT_HEIGHT -> viewportSize.y * this.value / 100.0f;
      default -> 0;
    };
  }

  public enum UnitType {
    PIXEL(true),
    PERCENT(false),
    VIEWPORT_WIDTH(true),
    VIEWPORT_HEIGHT(true),
    AUTO(false);

    final boolean fixed;

    UnitType(boolean fixed) {
      this.fixed = fixed;
    }

    public boolean isFixed() {
      return this.fixed;
    }

  }

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
