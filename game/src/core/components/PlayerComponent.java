package core.components;

import com.badlogic.gdx.Gdx;

import core.Component;
import core.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Component that marks an entity as playable.
 *
 * <p>This component is used to mark an entity as playable by the player.
 *
 * <p>It also contains a map of keys/buttons (Map-Key-Value) and functions (Map-Value). The {@link
 * core.systems.PlayerSystem} will trigger {@link #execute}. This method will check each entry in
 * the map and check if the given key is pressed. If so, the function registered to this key will be
 * executed.
 *
 * <p>Use {@link #registerFunction} to add a new function for a button press, for example, add
 * movement controls.
 *
 * <p>In the dungeon, keys/buttons are represented by an integer value.
 */
public class PlayerComponent extends Component {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private Map<Integer, Consumer<Entity>> functions;

    /**
     * Creates a new PlayerComponent.
     *
     * @param entity - the entity this component belongs to
     */
    public PlayerComponent(Entity entity) {
        super(entity);
        functions = new HashMap<>();
    }

    /**
     * Add a new function to this component.
     *
     * <p>If a function is already registered on this key, the old function will be replaced.
     *
     * @param key The key-value on which the function should be executed
     * @param function Function to execute if the key is pressed
     * @return Optional<Consumer<Entity>> The old function, if one was existing. Can be null.
     * @see com.badlogic.gdx.Gdx#input
     */
    public Optional<Consumer<Entity>> registerFunction(int key, Consumer<Entity> function) {
        Optional<Consumer<Entity>> oldFunction = Optional.ofNullable(functions.get(key));
        functions.put(key, function);
        return oldFunction;
    }

    /**
     * Remove the registered function on the given key.
     *
     * @param key Value of the key.
     */
    public void removeFunction(int key) {
        functions.remove(key);
    }

    /**
     * Will check each entry in the function map, and if the key is just pressed, the function will
     * be executed.
     */
    public void execute() {
        functions.forEach((k, f) -> execute(k, f));
    }

    private void execute(Integer key, Consumer<Entity> function) {
        if (Gdx.input.isKeyPressed(key)) function.accept(entity);
    }
}
