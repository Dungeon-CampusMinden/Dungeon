package contrib.crafting.result;

import com.badlogic.gdx.utils.JsonValue;

import core.Entity;

/**
 * A crafting result. This is the base class for all crafting results.
 *
 * <p>Crafting results are used to define the result of a crafting recipe. They are executed when a
 * recipe is crafted and can be used to add items to the player's inventory, add components to the
 * player's entity, etc...
 */
public abstract class CraftingResult {

    /**
     * The type of the crafting result. This is used to determine which type of crafting result this
     * is.
     */
    public enum Type {
        ITEM,
    }

    private final Type type;

    /**
     * Creates a new crafting result.
     *
     * @param type The type of the crafting result.
     */
    protected CraftingResult(Type type) {
        this.type = type;
    }

    /**
     * Returns the type of the crafting result.
     *
     * @return The type of the crafting result.
     */
    public Type type() {
        return type;
    }

    /**
     * Execute the result on the provided entity.
     *
     * <p>This will be called when the recipe is crafted. This can be used to add items to the
     * entity's inventory, add components to the entity, etc...
     *
     * @param entity Entity to execute the result on.
     */
    public abstract void execute(Entity entity);

    /**
     * Parses the crafting result from the specified JSON value.
     *
     * @param value The JSON value to parse.
     */
    public abstract void parse(JsonValue value);
}
