package core.level.utils;

/**
 * Represents the different available visual design themes for a dungeon environment. These labels
 * can be used to define the aesthetic style of the dungeon.
 */
public enum DesignLabel {
  /** The default dungeon-like design with a classic stone and brick appearance. */
  DEFAULT,

  /** A fiery theme featuring lava, heat, and volcanic visuals. */
  FIRE,

  /** A lush forest theme with greenery, trees, and natural elements. */
  FOREST,

  /** An icy theme with snow, frost, and cold, wintry visuals. */
  ICE,

  /** An ancient temple theme with mystical or sacred architectural elements. */
  TEMPLE,

  /** A dark, ominous theme with shadows and eerie atmosphere. */
  DARK,

  /** A very colorful, whimsical theme featuring a rainbow palette. */
  RAINBOW;

  /**
   * Converts the DesignLabel enum constant to its corresponding byte value.
   *
   * @return the byte value representing the DesignLabel
   */
  public byte toByte() {
    if (values().length > Byte.MAX_VALUE + 1) {
      throw new IllegalStateException(
          "Too many DesignLabel enum entries for byte encoding: " + values().length);
    }
    return (byte) this.ordinal();
  }

  /**
   * Converts a byte value to its corresponding DesignLabel enum constant.
   *
   * @param b the byte value representing the DesignLabel
   * @return the corresponding DesignLabel enum constant
   */
  public static DesignLabel fromByte(byte b) {
    return DesignLabel.values()[b];
  }
}
