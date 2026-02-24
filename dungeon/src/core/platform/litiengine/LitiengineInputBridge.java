package core.platform.litiengine;

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

    // Keyboard
    Input.keyboard().onKeyPressed(e -> {
      final int gdx = mapAwtKeyToGdx(e.getKeyCode());
      if (gdx != -1) {
        InputManager.notifyKeyDown(gdx);
      }
    });

    Input.keyboard().onKeyReleased(e -> {
      final int gdx = mapAwtKeyToGdx(e.getKeyCode());
      if (gdx != -1) {
        InputManager.notifyKeyUp(gdx);
      }
    });

    // Mouse
    Input.mouse().onPressed(e -> {
      final int btn = mapAwtButtonToGdx(e.getButton());
      if (btn != -1) {
        InputManager.notifyButtonDown(btn);
      }
    });

    Input.mouse().onReleased(e -> {
      final int btn = mapAwtButtonToGdx(e.getButton());
      if (btn != -1) {
        InputManager.notifyButtonUp(btn);
      }
    });
  }

  /** Maps AWT key codes to libGDX key codes. Only maps keys that are relevant to our game input handling.
   *
   * @param awtKey the AWT key code from the KeyEvent
   * @return the corresponding libGDX key code, or -1 if the key is not mapped/supported
   */
  private static int mapAwtKeyToGdx(int awtKey) {
    return switch (awtKey) {
      case KeyEvent.VK_W -> com.badlogic.gdx.Input.Keys.W;
      case KeyEvent.VK_A -> com.badlogic.gdx.Input.Keys.A;
      case KeyEvent.VK_S -> com.badlogic.gdx.Input.Keys.S;
      case KeyEvent.VK_D -> com.badlogic.gdx.Input.Keys.D;

      case KeyEvent.VK_UP -> com.badlogic.gdx.Input.Keys.UP;
      case KeyEvent.VK_DOWN -> com.badlogic.gdx.Input.Keys.DOWN;
      case KeyEvent.VK_LEFT -> com.badlogic.gdx.Input.Keys.LEFT;
      case KeyEvent.VK_RIGHT -> com.badlogic.gdx.Input.Keys.RIGHT;

      case KeyEvent.VK_SPACE -> com.badlogic.gdx.Input.Keys.SPACE;
      case KeyEvent.VK_ESCAPE -> com.badlogic.gdx.Input.Keys.ESCAPE;

      case KeyEvent.VK_F11 -> com.badlogic.gdx.Input.Keys.F11;
      case KeyEvent.VK_F3 -> com.badlogic.gdx.Input.Keys.F3;

      default -> -1;
    };
  }

  private static int mapAwtButtonToGdx(int awtButton) {
    return switch (awtButton) {
      case MouseEvent.BUTTON1 -> com.badlogic.gdx.Input.Buttons.LEFT;
      case MouseEvent.BUTTON2 -> com.badlogic.gdx.Input.Buttons.MIDDLE;
      case MouseEvent.BUTTON3 -> com.badlogic.gdx.Input.Buttons.RIGHT;
      default -> -1;
    };
  }
}
