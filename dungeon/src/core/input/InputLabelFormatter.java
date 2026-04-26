package core.input;

import java.util.Objects;

/**
 * Labels typed input codes for display.
 *
 * <p>Keyboard keys and mouse buttons can use overlapping integer codes. Callers must provide the
 * input type so labels are resolved through the matching input registry.
 */
public final class InputLabelFormatter {

  /** Supported input code categories. */
  public enum InputType {
    /** Keyboard key code. */
    KEYBOARD,
    /** Mouse button code. */
    MOUSE_BUTTON
  }

  /**
   * Typed input code for UI display.
   *
   * @param type input category
   * @param code raw input code
   */
  public record InputCode(InputType type, int code) {
    /**
     * Creates a typed input code.
     *
     * @param type input category
     * @param code raw input code
     */
    public InputCode {
      Objects.requireNonNull(type, "type must not be null");
    }

    /**
     * Returns the display label for this input code.
     *
     * @return input label
     */
    public String label() {
      return InputLabelFormatter.label(type, code);
    }
  }

  private InputLabelFormatter() {}

  /**
   * Creates a typed keyboard input code.
   *
   * @param keycode keyboard key code
   * @return typed input code
   */
  public static InputCode keyboard(int keycode) {
    return new InputCode(InputType.KEYBOARD, keycode);
  }

  /**
   * Creates a typed mouse button input code.
   *
   * @param button mouse button code
   * @return typed input code
   */
  public static InputCode mouseButton(int button) {
    return new InputCode(InputType.MOUSE_BUTTON, button);
  }

  /**
   * Labels a raw input code using the given type.
   *
   * @param type input category
   * @param code raw input code
   * @return input label
   */
  public static String label(InputType type, int code) {
    return switch (Objects.requireNonNull(type, "type must not be null")) {
      case KEYBOARD -> Keys.toString(code);
      case MOUSE_BUTTON -> MouseButtons.toString(code);
    };
  }
}
