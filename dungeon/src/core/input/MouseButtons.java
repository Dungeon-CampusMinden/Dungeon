package core.input;

/**
 * Constants for mouse button input codes.
 *
 * <p>MouseButtons provides a centralized registry of button code constants for common mouse inputs,
 * abstracting away platform-specific button code values. It maps human-readable names to their
 * corresponding button code integers.
 *
 * <p>Supported buttons:
 * <ul>
 *   <li>LEFT: Left mouse button (LMB)
 *   <li>RIGHT: Right mouse button (RMB)
 *   <li>MIDDLE: Middle mouse button (MMB)
 *   <li>BACK: Back mouse button (MOUSE_BACK)
 *   <li>FORWARD: Forward mouse button (MOUSE_FORWARD)
 * </ul>
 *
 * <p>This class is not instantiable; all members are static constants.
 */
public final class MouseButtons {

  public static final int LEFT = 0;
  public static final int RIGHT = 1;
  public static final int MIDDLE = 2;
  public static final int BACK = 3;
  public static final int FORWARD = 4;

  private MouseButtons() {}

  /**
   * Converts a button code to its human-readable string representation.
   *
   * <p>This method maps button code integers to their string names (e.g., LMB, RMB, MMB).
   * For known buttons, it returns the button name or abbreviation. For unknown button codes,
   * it returns a format string containing the raw button code (e.g., "MOUSE(5)").
   *
   * @param button the button code to convert
   * @return the human-readable string representation of the button code
   */
  public static String toString(int button) {
    return switch (button) {
      case LEFT -> "LMB";
      case RIGHT -> "RMB";
      case MIDDLE -> "MMB";
      case BACK -> "MOUSE_BACK";
      case FORWARD -> "MOUSE_FORWARD";
      default -> "MOUSE(" + button + ")";
    };
  }
}
