package de.fwatermann.dungine.input;

import static org.lwjgl.glfw.GLFW.*;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.window.GameWindow;
import java.util.HashSet;
import java.util.Set;

/**
 * The `Keyboard` class provides methods to check the state of keyboard keys.
 * It also listens for keyboard events and updates the state of keys accordingly.
 */
public class Keyboard implements EventListener {

  static {
    GameWindow.CURRENT_GAME.runOnMainThread(() -> {
      EventManager.getInstance().registerStaticListener(Keyboard.class);
    });
  }

  private static final Set<Integer> justPressedCheck = new HashSet<>();

  private Keyboard() {}

  /**
   * Checks if a key is currently pressed.
   *
   * @param key the key to check
   * @return true if the key is pressed, false otherwise
   */
  public static boolean keyPressed(int key) {
    return glfwGetKey(glfwGetCurrentContext(), key) == GLFW_PRESS;
  }

  /**
   * Checks if a key was just pressed. This method will return true only once per key press.
   *
   * @param key the key to check
   * @return true if the key was just pressed, false otherwise
   */
  public static boolean keyJustPressed(int key) {
    boolean pressed = glfwGetKey(glfwGetCurrentContext(), key) == GLFW_PRESS;
    if(pressed) {
      if(!justPressedCheck.contains(key)) {
        justPressedCheck.add(key);
        return true;
      }
      return false;
    } else {
      justPressedCheck.remove(key);
    }
    return false;
  }

  /**
   * Handles keyboard events and updates the state of keys.
   *
   * @param e the keyboard event
   */
  @EventHandler
  private static void onKey(KeyboardEvent e) {
    if(e.action == KeyboardEvent.KeyAction.RELEASE) {
      justPressedCheck.remove(e.key);
    }
  }

}
