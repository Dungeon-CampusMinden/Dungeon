package dungine.level.level3d.block;

/** The `BlockFace` enum represents the six faces of a block in a 3D level. */
public enum BlockFace {

  /** The face of the block that is facing upwards (y+). */
  UP,

  /** The face of the block that is facing downwards. (y-) */
  DOWN,

  /** The face of the block that is facing north. (z+) */
  NORTH,

  /** The face of the block that is facing south. (z-) */
  SOUTH,

  /** The face of the block that is facing west. (x-) */
  WEST,

  /** The face of the block that is facing east. (x+) */
  EAST;

  /**
   * Returns the opposite face of the block face.
   *
   * @return The opposite face of the block face.
   */
  public BlockFace opposite() {
    return switch (this) {
      case UP -> DOWN;
      case DOWN -> UP;
      case NORTH -> SOUTH;
      case SOUTH -> NORTH;
      case WEST -> EAST;
      case EAST -> WEST;
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }

  /**
   * Get the bitmask for the block face.
   *
   * @return The bitmask for the block face.
   */
  public byte bitMask() {
    return switch (this) {
      case UP -> 0b00100000;
      case DOWN -> 0b00010000;
      case NORTH -> 0b00001000;
      case SOUTH -> 0b00000100;
      case WEST -> 0b00000010;
      case EAST -> 0b00000001;
    };
  }

  /**
   * Get the block face from the bitmask.
   *
   * @param bitMask The bitmask to get the block face from.
   * @return The block face from the bitmask.
   */
  public static BlockFace fromBitMask(byte bitMask) {
    return switch (bitMask) {
      case 0b00100000 -> UP;
      case 0b00010000 -> DOWN;
      case 0b00001000 -> NORTH;
      case 0b00000100 -> SOUTH;
      case 0b00000010 -> WEST;
      case 0b00000001 -> EAST;
      default -> throw new IllegalArgumentException("Invalid bit mask: " + bitMask);
    };
  }
}
