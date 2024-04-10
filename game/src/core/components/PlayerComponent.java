package core.components;

import com.badlogic.gdx.Input;
import core.Component;
import core.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Marks an entity as playable by the player.
 *
 * <p>This component stores pairs of keystroke codes with an associated callback function. The
 * mappings can be added or changed via {@link #registerCallback} and removed via {@link
 * #removeCallback}. The codes for the buttons originate from {@link Input.Keys}.
 *
 * @see Input.Keys
 * @see core.systems.PlayerSystem
 */
public final class PlayerComponent implements Component {

  private final Map<Integer, InputData> callbacks;
  private int openDialogs = 0;

  /** Create a new PlayerComponent. */
  public PlayerComponent() {
    callbacks = new HashMap<>();
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
   * @return Optional<Consumer < Entity>> The old callback, if one was existing. Can be null.
   * @see com.badlogic.gdx.Gdx#input
   */
  public Optional<Consumer<Entity>> registerCallback(int key, final Consumer<Entity> callback) {
    Consumer<Entity> oldCallback = null;
    if (callbacks.containsKey(key)) {
      oldCallback = callbacks.get(key).callback();
    }
    callbacks.put(key, new InputData(true, callback));
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
   * @return Optional<Consumer < Entity>> The old callback, if one was existing. Can be null.
   */
  public Optional<Consumer<Entity>> registerCallback(
      int key, final Consumer<Entity> callback, boolean repeat, boolean pauseable) {
    Consumer<Entity> oldCallback = null;
    if (callbacks.containsKey(key)) {
      oldCallback = callbacks.get(key).callback();
    }
    callbacks.put(key, new InputData(repeat, callback, pauseable));
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
   * @return Optional<Consumer < Entity>> The old callback, if one was existing. Can be null.
   */
  public Optional<Consumer<Entity>> registerCallback(
      int key, final Consumer<Entity> callback, boolean repeat) {
    return this.registerCallback(key, callback, repeat, true);
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
    public InputData(boolean repeat, Consumer<Entity> callback) {
      this(repeat, callback, true);
    }
  }

  /** Increases the dialogue counter by 1. */
  public void incrementOpenDialogs() {
    openDialogs++;
  }

  /** Decreases the dialogue counter by 1. */
  public void decrementOpenDialogs() {
    openDialogs--;
  }

  /**
   * Indicates whether dialogs are currently open.
   *
   * @return true if dialogs are currently open, otherwise false
   */
  public boolean openDialogs() {
    return openDialogs > 0;
  }
}
