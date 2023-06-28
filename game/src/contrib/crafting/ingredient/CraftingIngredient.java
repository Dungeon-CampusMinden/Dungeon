package contrib.crafting.ingredient;

import com.badlogic.gdx.utils.JsonValue;

/**
 * A class which represents an ingredient in a crafting recipe.
 *
 * <p>The ingredient can be of different types, which are defined in the {@link Type} enum and their
 * respective classes.
 */
public abstract class CraftingIngredient {

    /** The different types of ingredients. */
    public enum Type {
        ITEM
    }

    protected Type type;

    /**
     * Create a new ingredient of the given type.
     *
     * @param type The type of the ingredient.
     */
    protected CraftingIngredient(Type type) {
        this.type = type;
    }

    /**
     * Get the type of the ingredient.
     *
     * @return The type of the ingredient.
     */
    public Type type() {
        return this.type;
    }

    /**
     * Check if the Ingredient matches the input.
     *
     * @return True if the ingredient matches the input, false otherwise.
     */
    public abstract boolean match(CraftingIngredient input);

    /**
     * Parse the ingredient from a JSON value.
     *
     * @param value JSON value to parse.
     */
    public abstract void parse(JsonValue value);
}
