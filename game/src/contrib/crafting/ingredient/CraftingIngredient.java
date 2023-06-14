package contrib.crafting.ingredient;

import com.badlogic.gdx.utils.JsonValue;

public abstract class CraftingIngredient {

    public enum Type {
        ITEM
    }

    protected Type type;

    public CraftingIngredient(Type type) {
        this.type = type;
    }

    /**
     * Get the type of the ingredient.
     *
     * @return The type of the ingredient.
     */
    public Type getType() {
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
