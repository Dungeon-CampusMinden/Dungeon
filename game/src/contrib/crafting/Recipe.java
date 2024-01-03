package contrib.crafting;

import java.util.Arrays;

/**
 * Crafting recipe that can be used to craft stuff.
 *
 * <p>A recipe consists of a set of ingredients and their corresponding results. It can be ordered
 * or unordered, depending on the required arrangement of ingredients.
 *
 * @param ordered true if the recipe requires the ingredients to be in a specific order, false
 *     otherwise
 * @param ingredients an array of {@link CraftingIngredient CraftingIngredients} representing the
 *     required ingredients
 * @param results an array of {@link CraftingResult CraftingResults} representing the resulting
 *     items or actions of the recipe
 */
public record Recipe(boolean ordered, CraftingIngredient[] ingredients, CraftingResult[] results) {

  /**
   * Checks if the recipe requires the ingredients to be in a specific order.
   *
   * @return true if the recipe is ordered, false otherwise
   */
  public boolean ordered() {
    return ordered;
  }

  /**
   * Checks if the given inputs can be used to craft the recipe. The inputs must match the required
   * ingredients in order and quantity.
   *
   * @param inputs an array of {@link CraftingIngredient CraftingIngredients} representing the
   *     provided inputs
   * @return true if the inputs can be used to craft the recipe, false otherwise
   */
  public boolean canCraft(final CraftingIngredient[] inputs) {
    if (inputs.length != this.ingredients.length) {
      return false;
    }

    if (this.ordered) {
      for (int i = 0; i < this.ingredients.length; i++) {
        if (!this.ingredients[i].match(inputs[i])) {
          return false;
        }
      }
      return true;
    }

    for (final CraftingIngredient ingredient : this.ingredients) {
      if (Arrays.stream(inputs).noneMatch(ingredient::match)) return false;
    }

    return true;
  }

  /**
   * Retrieves the array of required ingredients for the recipe.
   *
   * @return an array of {@link CraftingIngredient CraftingIngredients} objects representing the
   *     required ingredients
   */
  public CraftingIngredient[] ingredients() {
    return this.ingredients;
  }

  /**
   * Retrieves the array of results for the recipe.
   *
   * @return an array of {@link CraftingResult CraftingResults} objects representing the results of
   *     the recipe
   */
  public CraftingResult[] results() {
    return this.results;
  }
}
