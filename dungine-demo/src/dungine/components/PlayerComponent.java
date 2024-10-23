package dungine.components;

import de.fwatermann.dungine.ecs.Component;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.utils.functions.IVoidFunction1P;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The `PlayerComponent` class is a component that manages player-specific functionality within the
 * game. It allows for the registration and handling of key press callbacks, as well as tracking the
 * number of open dialogs.
 *
 * <p>Key functionalities include:
 *
 * <ul>
 *   <li>Registering and removing key press callbacks.
 *   <li>Handling repeated execution of callbacks while keys are pressed.
 *   <li>Tracking the number of open dialogs to manage game state.
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * PlayerComponent playerComponent = new PlayerComponent();
 * playerComponent.registerCallback(KeyEvent.VK_W, entity -> {
 *     // Move player forward
 * });
 * }</pre>
 */
public class PlayerComponent extends Component {

  private final Map<Integer, InputData> callbacks;
  private int openDialogs = 0;

  /** Create a new PlayerComponent. */
  public PlayerComponent() {
    super(false);
    this.callbacks = new HashMap<>();
  }

  /**
   * Registers a new callback for a key.
   *
   * <p>If a callback is already registered on this key, the old callback will be replaced.
   *
   * <p>The callback will be executed repeatedly while the key is pressed. Use {@link
   * #registerCallback(int, IVoidFunction1P, boolean)} to change this behavior.
   *
   * @param key The integer value of the key on which the callback should be executed.
   * @param callback The {@link Consumer} that contains the callback to execute if the key is
   *     pressed.
   * @return {@code Optional<Consumer<Entity>>} The old callback, if one was existing. Can be null.
   */
  public Optional<IVoidFunction1P<Entity>> registerCallback(
      int key, final IVoidFunction1P<Entity> callback) {
    IVoidFunction1P<Entity> oldCallback = null;
    if (this.callbacks.containsKey(key)) {
      oldCallback = this.callbacks.get(key).callback();
    }
    this.callbacks.put(key, new InputData(true, callback));
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
  public Optional<IVoidFunction1P<Entity>> registerCallback(
      int key, final IVoidFunction1P<Entity> callback, boolean repeat, boolean pauseable) {
    IVoidFunction1P<Entity> oldCallback = null;
    if (this.callbacks.containsKey(key)) {
      oldCallback = this.callbacks.get(key).callback();
    }
    this.callbacks.put(key, new InputData(repeat, callback));
    return Optional.ofNullable(oldCallback);
  }

  /**
   * Registers a new pauseable callback for a key.
   *
   * <p>If a callback is already registered on this key, the old callback will be replaced.
   *
   * <p>This method exists for compatibility reasons. Use {@link #registerCallback(int,
   * IVoidFunction1P, boolean, boolean)} instead.
   *
   * @param key The integer value of the key on which the callback should be executed.
   * @param callback The {@link Consumer} that contains the callback to execute if the key is
   *     pressed.
   * @param repeat If the callback should be executed repeatedly while the key is pressed.
   * @return {@code Optional<Consumer<Entity>>} The old callback, if one was existing. Can be null.
   */
  public Optional<IVoidFunction1P<Entity>> registerCallback(
      int key, final IVoidFunction1P<Entity> callback, boolean repeat) {
    return this.registerCallback(key, callback, repeat, false);
  }

  /**
   * Removes the registered callback on the given key.
   *
   * @param key The integer value of the key.
   */
  public void removeCallback(int key) {
    this.callbacks.remove(key);
  }

  /**
   * Gets the Key Configuration Map.
   *
   * @return A copy of the callback map.
   */
  public Map<Integer, InputData> callbacks() {
    return new HashMap<>(this.callbacks);
  }

  /** Increases the dialogue counter by 1. */
  public void incrementOpenDialogs() {
    this.openDialogs++;
  }

  /** Decreases the dialogue counter by 1. */
  public void decrementOpenDialogs() {
    this.openDialogs--;
  }

  /**
   * Indicates whether dialogs are currently open.
   *
   * @return true if dialogs are currently open, otherwise false
   */
  public boolean openDialogs() {
    return this.openDialogs > 0;
  }

  /**
   * Stores information for a Key Press Callback.
   *
   * @param repeat If the callback should be executed repeatedly while the key is pressed.
   * @param callback The {@link IVoidFunction1P} that contains the callback to execute if the key is
   *     pressed.
   */
  public record InputData(boolean repeat, IVoidFunction1P<Entity> callback) {}
}
