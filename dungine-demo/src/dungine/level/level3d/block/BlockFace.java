package dungine.level.level3d.block;

public enum BlockFace {

  UP, //Y+
  DOWN, //Y-
  NORTH, //Z+
  SOUTH, //Z-
  WEST, //X-
  EAST; //X+

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
