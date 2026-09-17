package rooms.systemRecovery.util;

/** Shared, high-contrast colors for the marked cells in the two-dimensional storage riddle. */
public final class StorageCellColors {

  private static final int ACTIVE = 0x4D7EA8FF;
  private static final int VALUE_ONE = 0x00FFFFFF;
  private static final int VALUE_TWO = 0xFFB000FF;
  private static final int VALUE_THREE = 0xFF00FFFF;

  private StorageCellColors() {}

  /**
   * @return the tint for an active but unmarked storage cell
   */
  public static int active() {
    return ACTIVE;
  }

  /**
   * Returns the tint associated with a required value.
   *
   * @param value required storage value
   * @return cyan for 1, orange for 2, magenta for 3, or the active tint for unknown values
   */
  public static int forValue(int value) {
    return switch (value) {
      case 1 -> VALUE_ONE;
      case 2 -> VALUE_TWO;
      case 3 -> VALUE_THREE;
      default -> ACTIVE;
    };
  }
}
