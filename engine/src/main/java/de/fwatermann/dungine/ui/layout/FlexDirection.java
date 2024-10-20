package de.fwatermann.dungine.ui.layout;

/** Enum representing the direction of the flexible items within a container. */
public enum FlexDirection {
  /** Items are placed in a row, from left to right. */
  ROW(0b00),

  /** Items are placed in a row, from right to left. */
  ROW_REVERSE(0b10),

  /** Items are placed in a column, from top to bottom. */
  COLUMN(0b01),

  /** Items are placed in a column, from bottom to top. */
  COLUMN_REVERSE(0b11);

  private final int flags;

  /**
   * Constructor for FlexDirection.
   *
   * @param flags The bitwise flags representing the direction.
   */
  FlexDirection(int flags) {
    this.flags = flags;
  }

  /**
   * Checks if the direction is a column.
   *
   * @return true if the direction is a column, false otherwise.
   */
  public boolean isColumn() {
    return (this.flags & 0b01) == 0b01;
  }

  /**
   * Checks if the direction is a row.
   *
   * @return true if the direction is a row, false otherwise.
   */
  public boolean isRow() {
    return !this.isColumn();
  }

  /**
   * Checks if the direction is reversed.
   *
   * @return true if the direction is reversed, false otherwise.
   */
  public boolean isReverse() {
    return (this.flags & 0b10) == 0b10;
  }
}
