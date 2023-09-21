package contrib.crafting;

/**
 * A class which represents an ingredient in a crafting recipe.
 *
 * <p>The ingredient can be of different types, which are defined in the {@link CraftingType} enum
 * and their respective classes.
 */
public interface CraftingIngredient {

    /**
     * Get the type of the ingredient.
     *
     * @return The type of the ingredient.
     */
    CraftingType ingredientType();

    /**
     * Check if the Ingredient matches the input.
     *
     * @return True if the ingredient matches the input, false otherwise.
     */
    boolean match(CraftingIngredient input);
}
