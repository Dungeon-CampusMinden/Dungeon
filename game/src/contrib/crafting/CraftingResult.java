package contrib.crafting;

import com.badlogic.gdx.utils.JsonValue;

import core.Entity;

/**
 * A crafting result. This is the base class for all crafting results.
 *
 * <p>Crafting results are used to define the result of a crafting recipe. They are executed when a
 * recipe is crafted and can be used to add items to the player's inventory, add components to the
 * player's entity, etc...
 */
public interface CraftingResult {

    /**
     * Returns the type of the crafting result.
     *
     * @return The type of the crafting result.
     */
    CraftingType resultType();

    /**
     * Execute the result on the provided entity. The provided entity is the entity that crafted the
     * recipe.
     *
     * <p>This will be called when the recipe is crafted. This can be used to add items to the
     * entity's inventory, add components to the entity, etc...
     *
     * <p>It should NOT be used to remove the used ingredients from the entity's inventory.
     *
     * @param entity Entity to execute the result on.
     */
    void executeCrafting(Entity entity);

    /**
     * Parses the crafting result from the specified JSON value.
     *
     * @param value The JSON value to parse.
     */
    void parseCraftingResult(JsonValue value);
}
