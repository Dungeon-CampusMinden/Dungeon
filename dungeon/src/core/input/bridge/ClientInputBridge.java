package core.input.bridge;

import core.input.Keys;
import core.input.MouseButtons;
import core.utils.InputManager;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Bridges client-side keyboard and mouse events into the backend-agnostic {@link InputManager}.
 *
 * <p>This bridge translates backend-specific AWT/LITIENGINE input events to the project's internal
 * input codes defined in {@link Keys} and {@link MouseButtons}.
 */
public final class ClientInputBridge {
  private static boolean installed = false;

  private ClientInputBridge() {}

  public static synchronized void install() {
    if (installed) {
      return;
    }
    installed = true;

    Input.keyboard()
      .onKeyPressed(
        e -> {
          final int keyCode = mapAwtKeyToInputCode(e.getKeyCode());
          if (keyCode != -1) {
            InputManager.notifyKeyDown(keyCode);
          }
        });

    Input.keyboard()
      .onKeyReleased(
        e -> {
          final int keyCode = mapAwtKeyToInputCode(e.getKeyCode());
          if (keyCode != -1) {
            InputManager.notifyKeyUp(keyCode);
          }
        });

    Input.keyboard()
      .onKeyTyped(
        e -> {
          if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
            InputManager.notifyKeyTyped(e.getKeyChar());
          }
        });

    Input.mouse()
      .onPressed(
        e -> {
          final int buttonCode = mapAwtButtonToInputCode(e.getButton());
          if (buttonCode != -1) {
            InputManager.notifyButtonDown(buttonCode);
          }
        });

    Input.mouse()
      .onReleased(
        e -> {
          final int buttonCode = mapAwtButtonToInputCode(e.getButton());
          if (buttonCode != -1) {
            InputManager.notifyButtonUp(buttonCode);
          }
        });
  }

  /**
   * Maps AWT key codes to the project's backend-agnostic input codes.
   *
   * <p>This mapping intentionally only covers gameplay-relevant pressed/released keys. Typed text
   * input is handled separately via {@code onKeyTyped}.
   */
  private static int mapAwtKeyToInputCode(int awtKey) {
    return switch (awtKey) {
      case KeyEvent.VK_W -> Keys.W;
      case KeyEvent.VK_A -> Keys.A;
      case KeyEvent.VK_S -> Keys.S;
      case KeyEvent.VK_D -> Keys.D;

      case KeyEvent.VK_UP -> Keys.UP;
      case KeyEvent.VK_DOWN -> Keys.DOWN;
      case KeyEvent.VK_LEFT -> Keys.LEFT;
      case KeyEvent.VK_RIGHT -> Keys.RIGHT;

      case KeyEvent.VK_SPACE -> Keys.SPACE;
      case KeyEvent.VK_ESCAPE -> Keys.ESCAPE;
      case KeyEvent.VK_ENTER -> Keys.ENTER;
      case KeyEvent.VK_BACK_SPACE -> Keys.BACKSPACE;
      case KeyEvent.VK_TAB -> Keys.TAB;
      case KeyEvent.VK_SHIFT -> Keys.SHIFT_LEFT;

      case KeyEvent.VK_0 -> Keys.NUM_0;
      case KeyEvent.VK_1 -> Keys.NUM_1;
      case KeyEvent.VK_2 -> Keys.NUM_2;
      case KeyEvent.VK_3 -> Keys.NUM_3;
      case KeyEvent.VK_4 -> Keys.NUM_4;
      case KeyEvent.VK_5 -> Keys.NUM_5;
      case KeyEvent.VK_6 -> Keys.NUM_6;
      case KeyEvent.VK_7 -> Keys.NUM_7;
      case KeyEvent.VK_8 -> Keys.NUM_8;
      case KeyEvent.VK_9 -> Keys.NUM_9;

      case KeyEvent.VK_F1 -> Keys.F1;
      case KeyEvent.VK_F2 -> Keys.F2;
      case KeyEvent.VK_F3 -> Keys.F3;
      case KeyEvent.VK_F4 -> Keys.F4;
      case KeyEvent.VK_F5 -> Keys.F5;
      case KeyEvent.VK_F6 -> Keys.F6;
      case KeyEvent.VK_F7 -> Keys.F7;
      case KeyEvent.VK_F8 -> Keys.F8;
      case KeyEvent.VK_F9 -> Keys.F9;
      case KeyEvent.VK_F10 -> Keys.F10;
      case KeyEvent.VK_F11 -> Keys.F11;
      case KeyEvent.VK_F12 -> Keys.F12;

      case KeyEvent.VK_E -> Keys.E;
      case KeyEvent.VK_I -> Keys.I;
      case KeyEvent.VK_Q -> Keys.Q;
      case KeyEvent.VK_M -> Keys.M;
      case KeyEvent.VK_C -> Keys.C;
      case KeyEvent.VK_G -> Keys.G;
      case KeyEvent.VK_H -> Keys.H;
      case KeyEvent.VK_J -> Keys.J;
      case KeyEvent.VK_K -> Keys.K;
      case KeyEvent.VK_L -> Keys.L;
      case KeyEvent.VK_O -> Keys.O;
      case KeyEvent.VK_P -> Keys.P;
      case KeyEvent.VK_X -> Keys.X;
      case KeyEvent.VK_V -> Keys.V;
      case KeyEvent.VK_Z -> Keys.Z;

      default -> -1;
    };
  }

  /** Maps AWT mouse button codes to the project's backend-agnostic input codes. */
  private static int mapAwtButtonToInputCode(int awtButton) {
    return switch (awtButton) {
      case MouseEvent.BUTTON1 -> MouseButtons.LEFT;
      case MouseEvent.BUTTON2 -> MouseButtons.MIDDLE;
      case MouseEvent.BUTTON3 -> MouseButtons.RIGHT;
      default -> -1;
    };
  }
}
