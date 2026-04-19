package core.platform.client.input;

import core.input.Keys;
import core.input.MouseButtons;
import core.utils.InputManager;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Bridges LITIENGINE input events to the core InputManager.
 *
 * <p>ClientInputBridge connects the LITIENGINE input system to the core game engine's InputManager,
 * translating AWT key and mouse events into the engine's input code system. It provides a single
 * installation point that registers all necessary event listeners.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Listening for keyboard press, release, and type events from LITIENGINE
 *   <li>Listening for mouse button press and release events from LITIENGINE
 *   <li>Mapping AWT key codes and mouse button codes to engine-specific input codes
 *   <li>Forwarding mapped events to InputManager for distribution to game systems
 * </ul>
 *
 * <p>The bridge supports a comprehensive set of keyboard inputs (letters, numbers, arrows, function keys,
 * modifiers) and mouse buttons (left, right, middle).
 *
 * <p>Installation is thread-safe and idempotent: calling install() multiple times is safe.
 *
 * <p>This class is not instantiable; all members are static.
 */
public final class ClientInputBridge {

  private static boolean installed = false;

  private ClientInputBridge() {}

  /**
   * Installs the input bridge by registering event listeners with LITIENGINE.
   *
   * <p>This method registers the following listeners:
   * <ul>
   *   <li>Keyboard press listener - translates key presses to InputManager key down events
   *   <li>Keyboard release listener - translates key releases to InputManager key up events
   *   <li>Keyboard type listener - forwards typed characters to InputManager
   *   <li>Mouse button press listener - translates button presses to InputManager button down events
   *   <li>Mouse button release listener - translates button releases to InputManager button-up events
   * </ul>
   *
   * <p>This method is thread-safe and idempotent: calling it multiple times has no additional effect
   * after the first call (later calls are no-ops due to the installed flag).
   */
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
      case KeyEvent.VK_DELETE -> Keys.DELETE;
      case KeyEvent.VK_TAB -> Keys.TAB;
      case KeyEvent.VK_SHIFT -> Keys.SHIFT_LEFT;
      case KeyEvent.VK_COMMA -> Keys.COMMA;

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

  private static int mapAwtButtonToInputCode(int awtButton) {
    return switch (awtButton) {
      case MouseEvent.BUTTON1 -> MouseButtons.LEFT;
      case MouseEvent.BUTTON2 -> MouseButtons.MIDDLE;
      case MouseEvent.BUTTON3 -> MouseButtons.RIGHT;
      default -> -1;
    };
  }
}
