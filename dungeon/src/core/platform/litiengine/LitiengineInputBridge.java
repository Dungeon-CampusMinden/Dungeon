package core.platform.litiengine;

import core.input.Keys;
import core.input.MouseButtons;
import core.utils.InputManager;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public final class LitiengineInputBridge {

  private static boolean installed = false;

  private LitiengineInputBridge() {}

  public static synchronized void install() {
    if (installed) return;
    installed = true;

    // Keyboard pressed/released for gameplay input
    Input.keyboard().onKeyPressed(
      e -> {
        final int gdx = mapAwtKeyToGdx(e.getKeyCode());
        if (gdx != -1) {
          InputManager.notifyKeyDown(gdx);
        }
      });

    Input.keyboard().onKeyReleased(
      e -> {
        final int gdx = mapAwtKeyToGdx(e.getKeyCode());
        if (gdx != -1) {
          InputManager.notifyKeyUp(gdx);
        }
      });

    // Keyboard typed for text input (dialogs, future UI widgets)
    Input.keyboard().onKeyTyped(
      e -> {
        if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
          InputManager.notifyKeyTyped(e.getKeyChar());
        }
      });

    // Mouse
    Input.mouse().onPressed(
      e -> {
        final int btn = mapAwtButtonToGdx(e.getButton());
        if (btn != -1) {
          InputManager.notifyButtonDown(btn);
        }
      });

    Input.mouse().onReleased(
      e -> {
        final int btn = mapAwtButtonToGdx(e.getButton());
        if (btn != -1) {
          InputManager.notifyButtonUp(btn);
        }
      });
  }

  /**
   * Maps AWT key codes to engine-agnostic key codes.
   *
   * <p>This mapping intentionally only covers gameplay-relevant pressed/released keys. Typed text
   * input is handled separately via {@code onKeyTyped}.
   */
  private static int mapAwtKeyToGdx(int awtKey) {
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

      case KeyEvent.VK_F11 -> Keys.F11;
      case KeyEvent.VK_F3 -> Keys.F3;

      // Keys used by our KeyboardConfig(s)
      case KeyEvent.VK_E -> Keys.E;
      case KeyEvent.VK_I -> Keys.I;
      case KeyEvent.VK_Q -> Keys.Q;
      case KeyEvent.VK_P -> Keys.P;
      case KeyEvent.VK_M -> Keys.M;
      case KeyEvent.VK_J -> Keys.J;
      case KeyEvent.VK_H -> Keys.H;
      case KeyEvent.VK_G -> Keys.G;
      case KeyEvent.VK_O -> Keys.O;
      case KeyEvent.VK_C -> Keys.C;
      case KeyEvent.VK_K -> Keys.K;
      case KeyEvent.VK_L -> Keys.L;
      case KeyEvent.VK_X -> Keys.X;
      case KeyEvent.VK_COMMA -> Keys.COMMA;
      case KeyEvent.VK_PERIOD -> Keys.PERIOD;

      default -> -1;
    };
  }

  private static int mapAwtButtonToGdx(int awtButton) {
    return switch (awtButton) {
      case MouseEvent.BUTTON1 -> MouseButtons.LEFT;
      case MouseEvent.BUTTON2 -> MouseButtons.MIDDLE;
      case MouseEvent.BUTTON3 -> MouseButtons.RIGHT;
      default -> -1;
    };
  }
}
