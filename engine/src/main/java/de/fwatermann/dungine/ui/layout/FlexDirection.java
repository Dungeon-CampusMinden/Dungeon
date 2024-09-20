package de.fwatermann.dungine.ui.layout;

public enum FlexDirection {
  ROW(0b00),
  ROW_REVERSE(0b10),
  COLUMN(0b00),
  COLUMN_REVERSE(0b10);

  private final int flags;

  FlexDirection(int flags) {
    this.flags = flags;
  }

  public boolean isColumn() {
    return (this.flags & 0b01) == 0b01;
  }

  public boolean isRow() {
    return !this.isColumn();
  }

  public boolean isReverse() {
    return (this.flags & 0b10) == 0b10;
  }
}
