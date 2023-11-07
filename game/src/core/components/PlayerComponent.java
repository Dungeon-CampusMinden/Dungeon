package core.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import core.Component;
import core.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Mark an entity as playable by the player.
 *
 * <p>This component stores pairs of keystroke codes with an associated callback function. The
 * mappings can be added or changed via {@link #registerCallback} and deleted via {@link
 * #removeCallback}. The codes for the buttons originate from {@link Input.Keys}
 *
 * <p>The {@link core.systems.PlayerSystem} invokes the {@link #execute} method of this component,
 * which invokes for each stored tuple the associated callback if the corresponding button was
 * pressed.
 *
 * @see Input.Keys
 * @see core.systems.PlayerSystem
 */
public final class PlayerComponent implements Component {

    public record InputData(boolean repeat, Consumer<Entity> callback, boolean pausable) {
        public InputData(boolean repeat, Consumer<Entity> callback) {
            this(repeat, callback, true);
        }
    }

    private final Map<Integer, InputData> callbacks;

    /** Create a new PlayerComponent and add it to the associated entity. */
    public PlayerComponent() {
        callbacks = new HashMap<>();
    }

    /**
     * Register a new callback for a key.
     *
     * <p>If a callback is already registered on this key, the old callback will be replaced.
     *
     * <p>The callback will be executed repeatedly while the key is pressed. Use {@link
     * #registerCallback(int, Consumer, boolean)} to change this behaviour.
     *
     * @param key The integer value of the key on which the callback should be executed.
     * @param callback The {@link Consumer} that contains the callback to execute if the key is
     *     pressed.
     * @return Optional<Consumer<Entity>> The old callback, if one was existing. Can be null.
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
     * Register a new callback for a key.
     *
     * <p>If a callback is already registered on this key, the old callback will be replaced.
     *
     * @param key The integer value of the key on which the callback should be executed.
     * @param callback The {@link Consumer} that contains the callback to execute if the key is
     * @param repeat If the callback should be executed repeatedly while the key is pressed.
     * @param pausable If the callback should be executed while the game is paused.
     * @return Optional<Consumer<Entity>> The old callback, if one was existing. Can be null.
     */
    public Optional<Consumer<Entity>> registerCallback(
            int key, final Consumer<Entity> callback, boolean repeat, boolean pausable) {
        Consumer<Entity> oldCallback = null;
        if (callbacks.containsKey(key)) {
            oldCallback = callbacks.get(key).callback();
        }
        callbacks.put(key, new InputData(repeat, callback, pausable));
        return Optional.ofNullable(oldCallback);
    }

    /**
     * Register a new pausable callback for a key.
     *
     * <p>If a callback is already registered on this key, the old callback will be replaced.
     *
     * <p>This exists for compatibility reasons. Use {@link #registerCallback(int, Consumer,
     * boolean, boolean)} instead.
     *
     * @param key The integer value of the key on which the callback should be executed.
     * @param callback The {@link Consumer} that contains the callback to execute if the key is
     * @param repeat If the callback should be executed repeatedly while the key is pressed.
     * @return Optional<Consumer<Entity>> The old callback, if one was existing. Can be null.
     */
    public Optional<Consumer<Entity>> registerCallback(
            int key, final Consumer<Entity> callback, boolean repeat) {
        return this.registerCallback(key, callback, repeat, true);
    }

    /**
     * Remove the registered callback on the given key.
     *
     * @param key The integer value of the key.
     * @see com.badlogic.gdx.Gdx#input
     */
    public void removeCallback(int key) {
        callbacks.remove(key);
    }

    /**
     * Execute the callback function registered to a key when it is pressed.
     *
     * <p>The callbacks are executed only if the game is not paused or if the callback is not
     * pausable.
     *
     * @param entity associated entity of this component.
     * @param paused if the game is paused or not.
     */
    public void execute(final Entity entity, boolean paused) {
        this.callbacks.forEach(
                (key, value) -> {
                    if (!paused || !value.pausable) {
                        this.execute(entity, key, value);
                    }
                });
    }

    private void execute(Entity entity, int key, final InputData data) {
        if ((!data.repeat()
                        && (Gdx.input.isKeyJustPressed(key) || Gdx.input.isButtonJustPressed(key)))
                || (data.repeat()
                        && (Gdx.input.isKeyPressed(key) || Gdx.input.isButtonJustPressed(key)))) {
            data.callback().accept(entity);
        }
    }
}
