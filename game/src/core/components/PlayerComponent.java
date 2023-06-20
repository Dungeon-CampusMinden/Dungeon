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
public final class PlayerComponent extends Component {
    private final Map<Integer, Consumer<Entity>> callbacks;

    /**
     * Create a new PlayerComponent and add it to the associated entity.
     *
     * @param entity associated entity
     */
    public PlayerComponent(final Entity entity) {
        super(entity);
        callbacks = new HashMap<>();
    }

    /**
     * Register a new callback for a key.
     *
     * <p>If a callback is already registered on this key, the old callback will be replaced.
     *
     * @param key The integer value of the key on which the callback should be executed.
     * @param callback The {@link Consumer} that contains the callback to execute if the key is
     *     pressed.
     * @return Optional<Consumer<Entity>> The old callback, if one was existing. Can be null.
     * @see com.badlogic.gdx.Gdx#input
     */
    public Optional<Consumer<Entity>> registerCallback(int key, final Consumer<Entity> callback) {
        Optional<Consumer<Entity>> oldCallback = Optional.ofNullable(callbacks.get(key));
        callbacks.put(key, callback);
        return oldCallback;
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

    /** Execute the callback function registered to a key when it is pressed.. */
    public void execute() {
        callbacks.forEach(this::execute);
    }

    private void execute(int key, final Consumer<Entity> callback) {
        if (Gdx.input.isKeyPressed(key)) callback.accept(entity);
    }
}
