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
 * <p>It contains a map of keys (as integers) (Map-Key-Value) and {@link Consumer<Entity>}
 * (Map-Value).
 *
 * <p>The {@link core.systems.PlayerSystem} will trigger {@link #execute}. This method will check
 * each entry in the map and verify if the corresponding key is pressed. If so, the function
 * registered to this key will be executed.
 *
 * <p>Use {@link #registerFunction} to add a new function for a button press.
 *
 * <p>In the dungeon, keys/buttons are represented by an integer value.
 *
 * @see Input.Keys
 * @see core.systems.PlayerSystem
 */
public final class PlayerComponent extends Component {
    private final Map<Integer, Consumer<Entity>> functions;

    /**
     * Create a new PlayerComponent and add it to the associated entity.
     *
     * @param entity associated entity
     */
    public PlayerComponent(final Entity entity) {
        super(entity);
        functions = new HashMap<>();
    }

    /**
     * Add a new function to this component.
     *
     * <p>If a function is already registered on this key, the old function will be replaced.
     *
     * @param key The integer value of the key on which the function should be executed.
     * @param function The {@link Consumer} that contains the function to execute if the key is
     *     pressed.
     * @return Optional<Consumer<Entity>> The old function, if one was existing. Can be null.
     * @see com.badlogic.gdx.Gdx#input
     */
    public Optional<Consumer<Entity>> registerFunction(
            final int key, final Consumer<Entity> function) {
        Optional<Consumer<Entity>> oldFunction = Optional.ofNullable(functions.get(key));
        functions.put(key, function);
        return oldFunction;
    }

    /**
     * Remove the registered function on the given key.
     *
     * @param key The integer value of the key.
     * @see com.badlogic.gdx.Gdx#input
     */
    public void removeFunction(final int key) {
        functions.remove(key);
    }

    /**
     * This method will check each entry in the function map, and if the key is just pressed, the
     * function will be executed.
     */
    public void execute() {
        functions.forEach(this::execute);
    }

    private void execute(final int key, final Consumer<Entity> function) {
        if (Gdx.input.isKeyPressed(key)) function.accept(entity);
    }
}
