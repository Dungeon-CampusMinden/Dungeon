package core.components;

import core.Component;
import core.Entity;
import core.systems.InputSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This component stores pairs of keystroke codes with an associated callback function. The mappings
 * can be added or changed via {@link #registerCallback} and removed via {@link
 * #removeCallback(int)}. The codes for the buttons originate from {@link
 * com.badlogic.gdx.Input.Keys}.
 *
 * <p>It is used by the {@link InputSystem} to process input events for the player.
 */
public class InputComponent implements Component {

  private final Map<Integer, InputData> callbacks;
  private boolean deactivate = false;

  /** Create a new InputComponent. */
  public InputComponent() {
    callbacks = new HashMap<>();
  }

  /**
   * Enables or disables the controls.
   *
   * @param deactivate true to disable player controls; false to enable them
   */
  public void deactivateControls(boolean deactivate) {
    this.deactivate = deactivate;
  }

  /**
   * Returns whether the controls are currently deactivated.
   *
   * @return true if controls are disabled; false if they are active
   */
  public boolean deactivateControls() {
    return this.deactivate;
  }

  /**
   * Registers a new callback for a key.
   *
   * <p>If a callback is already registered on this key, the old callback will be replaced.
   *
   * <p>The callback will be executed repeatedly while the key is pressed. Use {@link
   * #registerCallback(int, Consumer, boolean)} to change this behavior.
   *
   * @param key The integer value of the key on which the callback should be executed.
   * @param callback The {@link Consumer} that contains the callback to execute if the key is
   *     pressed.
   * @return {@code Optional<Consumer<Entity>>} The old callback, if one was existing. Can be null.
   * @see com.badlogic.gdx.Gdx#input
   */
  public Optional<Consumer<Entity>> registerCallback(int key, final Consumer<Entity> callback) {
    Consumer<Entity> oldCallback = null;
    if (callbacks.containsKey(key)) {
      oldCallback = callbacks.get(key).callback();
    }
    callbacks.put(key, new InputComponent.InputData(true, callback));
    return Optional.ofNullable(oldCallback);
  }

  /**
   * Registers a new callback for a key.
   *
   * <p>If a callback is already registered on this key, the old callback will be replaced.
   *
   * @param key The integer value of the key on which the callback should be executed.
   * @param callback The {@link Consumer} that contains the callback to execute if the key is
   *     pressed.
   * @param repeat If the callback should be executed repeatedly while the key is pressed.
   * @param pauseable If the callback should be executed while the game is paused.
   * @return {@code Optional<Consumer<Entity>>} The old callback, if one was existing. Can be null.
   */
  public Optional<Consumer<Entity>> registerCallback(
      int key, final Consumer<Entity> callback, boolean repeat, boolean pauseable) {
    Consumer<Entity> oldCallback = null;
    if (callbacks.containsKey(key)) {
      oldCallback = callbacks.get(key).callback();
    }
    callbacks.put(key, new InputComponent.InputData(repeat, callback, pauseable));
    return Optional.ofNullable(oldCallback);
  }

  /**
   * Registers a new pauseable callback for a key.
   *
   * <p>If a callback is already registered on this key, the old callback will be replaced.
   *
   * <p>This method exists for compatibility reasons. Use {@link #registerCallback(int, Consumer,
   * boolean, boolean)} instead.
   *
   * @param key The integer value of the key on which the callback should be executed.
   * @param callback The {@link Consumer} that contains the callback to execute if the key is
   *     pressed.
   * @param repeat If the callback should be executed repeatedly while the key is pressed.
   * @return {@code Optional<Consumer<Entity>>} The old callback, if one was existing. Can be null.
   */
  public Optional<Consumer<Entity>> registerCallback(
      int key, final Consumer<Entity> callback, boolean repeat) {
    return this.registerCallback(key, callback, repeat, false);
  }

  /**
   * Removes the registered callback on the given key.
   *
   * @param key The integer value of the key.
   * @see com.badlogic.gdx.Gdx#input
   */
  public void removeCallback(int key) {
    callbacks.remove(key);
  }

  /** Removes all registered callbacks. */
  public void removeCallbacks() {
    callbacks.clear();
  }

  /**
   * Gets the Key Configuration Map.
   *
   * @return A copy of the callback map.
   */
  public Map<Integer, InputData> callbacks() {
    return new HashMap<>(callbacks);
  }

  /**
   * Stores information for a Key Press Callback.
   *
   * @param repeat If the callback should be executed repeatedly while the key is pressed.
   * @param callback The {@link Consumer} that contains the callback to execute if the key is
   *     pressed.
   * @param pauseable If the callback should be executed while the game is paused.
   */
  public record InputData(boolean repeat, Consumer<Entity> callback, boolean pauseable) {
    /**
     * WTF? .
     *
     * @param repeat foo
     * @param callback foo
     */
    public InputData(boolean repeat, Consumer<Entity> callback) {
      this(repeat, callback, false);
    }
  }
}
