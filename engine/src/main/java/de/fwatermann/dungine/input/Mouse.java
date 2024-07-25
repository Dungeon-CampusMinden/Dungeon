package de.fwatermann.dungine.input;

import static org.lwjgl.glfw.GLFW.*;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.MouseButtonEvent;
import java.util.HashSet;
import java.util.Set;
import org.joml.Vector2i;

/**
 * A class that provides methods to interact with the mouse.
 */
public class Mouse implements EventListener {

  static {
    EventManager.getInstance().registerStaticListener(Mouse.class);
  }

  private static final Set<Integer> justPressedCheck = new HashSet<>();

  /**
   * Check if a button is pressed.
   * @param button the button to check
   * @return true if the button is pressed, false otherwise
   */
  public static boolean buttonPressed(int button) {
    return glfwGetMouseButton(glfwGetCurrentContext(), button) == GLFW_PRESS;
  }

  /**
   * Check if a button was just pressed. This method will return true only once per button press.
   * @param button the button to check
   * @return true if the button was just pressed, false otherwise
   */
  public static boolean buttonJustPressed(int button) {
    boolean pressed = glfwGetMouseButton(glfwGetCurrentContext(), button) == GLFW_PRESS;
    if(pressed) {
      if(!justPressedCheck.contains(button)) {
        justPressedCheck.add(button);
        return true;
      }
      return false;
    } else {
      justPressedCheck.remove(button);
    }
    return false;
  }

  /**
   * Get the current mouse position.
   * @return the current mouse position as a Vector2i object
   */
  public static Vector2i getMousePosition() {
    double[] x = new double[1];
    double[] y = new double[1];
    glfwGetCursorPos(glfwGetCurrentContext(), x, y);
    return new Vector2i((int) x[0], (int) y[0]);
  }

  /**
   * Set the mouse position.
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public static void setMousePosition(int x, int y) {
    glfwSetCursorPos(glfwGetCurrentContext(), x, y);
  }

  /**
   * Set the mouse position.
   * @param pos the position as a Vector2i
   */
  public static void setMousePosition(Vector2i pos) {
    setMousePosition(pos.x, pos.y);
  }

  @EventHandler
  private static void onMouseButton(MouseButtonEvent event) {
    if(event.action == MouseButtonEvent.MouseButtonAction.RELEASE) {
      justPressedCheck.remove(event.button);
    }
  }

}
