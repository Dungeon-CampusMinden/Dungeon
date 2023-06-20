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
 * mappings can be added or changed via {@link #registerFunction} and deleted via {@link
 * #removeFunction}. The codes for the buttons originate from {@link Input.Keys}
 *
 * <p>The {@link core.systems.PlayerSystem} invokes the {@link #execute} method of this component,
 * which invokes for each stored tuple the associated callback if the corresponding button was
 * pressed.
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
    public Optional<Consumer<Entity>> registerFunction(int key, final Consumer<Entity> function) {
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
    public void removeFunction(int key) {
        functions.remove(key);
    }

    /**
     * This method will check each entry in the function map, and if the key is just pressed, the
     * function will be executed.
     */
    public void execute() {
        functions.forEach(this::execute);
    }

    private void execute(int key, final Consumer<Entity> function) {
        if (Gdx.input.isKeyPressed(key)) function.accept(entity);
    }
}
