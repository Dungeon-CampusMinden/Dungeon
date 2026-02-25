package core.input;

/**
 * Engine-agnostic mouse button codes.
 *
 * <p>Values are aligned with libGDX Input.Buttons to keep existing config files compatible.
 */
public final class MouseButtons {
  public static final int LEFT = 0;
  public static final int RIGHT = 1;
  public static final int MIDDLE = 2;
  public static final int BACK = 3;
  public static final int FORWARD = 4;

  private MouseButtons() {}

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
