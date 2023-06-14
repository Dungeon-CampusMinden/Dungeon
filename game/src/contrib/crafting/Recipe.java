package contrib.crafting;

import contrib.crafting.ingredient.CraftingIngredient;
import contrib.crafting.result.CraftingResult;

import java.util.Arrays;

public class Recipe {

    private final boolean ordered;
    private final CraftingIngredient[] ingredients;
    private final CraftingResult[] results;

    public Recipe(boolean ordered, CraftingIngredient[] ingredients, CraftingResult[] results) {
        this.ordered = ordered;
        this.ingredients = ingredients;
        this.results = results;
    }

    /**
     * Check if the recipe is ordered.
     *
     * @return True if the recipe is ordered, false otherwise.
     */
    public boolean isOrdered() {
        return ordered;
    }

    public boolean canCraft(CraftingIngredient[] inputs) {
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
}
