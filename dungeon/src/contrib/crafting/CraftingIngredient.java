package contrib.crafting;

/**
 * Represents an ingredient in a crafting recipe.
 *
 * <p>The ingredient can be of different types, which are defined in the {@link CraftingType} enum
 * and their respective classes.
 */
public interface CraftingIngredient {

  /**
   * Check if the Ingredient matches the input.
   *
   * @param input The input to match.
   * @return True if the ingredient matches the input, false otherwise.
   */
  boolean match(final CraftingIngredient input);

  /**
   * Sets the amount of the ingredient.
   *
   * @param count The amount to set.
   */
  void setAmount(int count);

  /**
   * Gets the amount of the ingredient.
   *
   * @return The amount of the ingredient.
   */
  int getAmount();
}
