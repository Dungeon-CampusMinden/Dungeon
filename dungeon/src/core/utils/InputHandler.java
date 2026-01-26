package core.utils;

import com.badlogic.gdx.Gdx;
import core.Game;

/**
 * Utility class for handling input with consideration for UI focus.
 */
public class InputHandler {

  private static boolean hasKeyboardFocus(){
    return Game.stage().map(s -> s.getKeyboardFocus() != null).orElse(false);
  }

  /**
   * Check if a key is pressed, ignoring input if a UI element has keyboard focus (such as a text field).
   * @param key The key to check.
   * @return True if the key is pressed and no UI element has keyboard focus, false otherwise.
   */
  public static boolean isKeyPressed(int key){
    return !hasKeyboardFocus() && Gdx.input.isKeyPressed(key);
  }

  /**
   * Check if a key was just pressed, ignoring input if a UI element has keyboard focus (such as a text field).
   * @param key The key to check.
   * @return True if the key was just pressed and no UI element has keyboard focus, false otherwise.
   */
  public static boolean isKeyJustPressed(int key){
    return !hasKeyboardFocus() && Gdx.input.isKeyJustPressed(key);
  }

}
