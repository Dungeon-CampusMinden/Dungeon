package contrib.crafting;

import static org.junit.Assert.*;

import contrib.crafting.ingredient.CraftingIngredient;
import contrib.crafting.ingredient.CraftingItemIngredient;
import contrib.crafting.result.CraftingItemResult;
import contrib.crafting.result.CraftingResult;
import contrib.utils.components.item.Item;

import org.junit.Test;

import java.util.Optional;

public class CraftingTest {

    @Test
    public void testFindRecipeWithNoInputs() {
        assertTrue(
                "No Recipe should be found with no ingredients.",
                Crafting.getRecipeByIngredients(new CraftingItemIngredient[0]).isEmpty());
    }

    @Test
    public void testRecipeFoundUnordered() {
        // Prepare Recipe
        CraftingIngredient[] recipeIngredient = {
            new CraftingItemIngredient(Item.WATER_BOTTLE, 1),
            new CraftingItemIngredient(Item.MUSHROOM_RED, 1),
            new CraftingItemIngredient(Item.FLOWER_RED, 1),
        };
        CraftingResult[] recipeResults = {new CraftingItemResult(Item.HEALTH_POTION, 1)};
        Recipe recipe = new Recipe(false, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipe);

        // Test

        CraftingItemIngredient[] ingredients = {
            new CraftingItemIngredient(Item.FLOWER_RED, 1),
            new CraftingItemIngredient(Item.MUSHROOM_RED, 1),
            new CraftingItemIngredient(Item.WATER_BOTTLE, 1),
        };

        Optional<Recipe> foundRecipe = Crafting.getRecipeByIngredients(ingredients);

        assertFalse("There should be a recipe.", foundRecipe.isEmpty());
        assertEquals("The found recipe is the correct recipe", recipe, foundRecipe.get());

        // Cleanup
        Crafting.clearRecipes();
    }

    @Test
    public void testRecipeOrdered_CorrectOrder() {
        // Prepare Recipe
        CraftingIngredient[] recipeIngredient = {
            new CraftingItemIngredient(Item.WATER_BOTTLE, 1),
            new CraftingItemIngredient(Item.MUSHROOM_RED, 1),
            new CraftingItemIngredient(Item.FLOWER_RED, 1),
        };
        CraftingResult[] recipeResults = {new CraftingItemResult(Item.HEALTH_POTION, 1)};
        Recipe recipe = new Recipe(true, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipe);

        // Test

        CraftingItemIngredient[] ingredients = {
            new CraftingItemIngredient(Item.WATER_BOTTLE, 1),
            new CraftingItemIngredient(Item.MUSHROOM_RED, 1),
            new CraftingItemIngredient(Item.FLOWER_RED, 1),
        };

        Optional<Recipe> foundRecipe = Crafting.getRecipeByIngredients(ingredients);

        assertFalse("There should be a recipe.", foundRecipe.isEmpty());
        assertEquals("The found recipe is the correct recipe", recipe, foundRecipe.get());

        // Cleanup
        Crafting.clearRecipes();
    }

    @Test
    public void testRecipeOrdered_IncorrectOrder() {
        // Prepare Recipe
        CraftingIngredient[] recipeIngredient = {
            new CraftingItemIngredient(Item.WATER_BOTTLE, 1),
            new CraftingItemIngredient(Item.MUSHROOM_RED, 1),
            new CraftingItemIngredient(Item.FLOWER_RED, 1),
        };
        CraftingResult[] recipeResults = {new CraftingItemResult(Item.HEALTH_POTION, 1)};
        Recipe recipe = new Recipe(true, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipe);

        // Test

        CraftingItemIngredient[] ingredients = {
            new CraftingItemIngredient(Item.MUSHROOM_RED, 1),
            new CraftingItemIngredient(Item.FLOWER_RED, 1),
            new CraftingItemIngredient(Item.WATER_BOTTLE, 1),
        };

        Optional<Recipe> foundRecipe = Crafting.getRecipeByIngredients(ingredients);

        assertTrue("There should be no recipe.", foundRecipe.isEmpty());

        // Cleanup
        Crafting.clearRecipes();
    }

    @Test
    public void testPrioritizeOrderedRecipes() {
        // Prepare Recipe
        CraftingIngredient[] recipeIngredient = {
            new CraftingItemIngredient(Item.WATER_BOTTLE, 1),
            new CraftingItemIngredient(Item.MUSHROOM_RED, 1),
            new CraftingItemIngredient(Item.FLOWER_RED, 1),
        };
        CraftingResult[] recipeResults = {new CraftingItemResult(Item.HEALTH_POTION, 1)};
        Recipe recipeOrdered = new Recipe(true, recipeIngredient, recipeResults);
        Recipe recipeUnordered = new Recipe(false, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipeUnordered);
        Crafting.addRecipe(recipeOrdered);

        // Test
        CraftingItemIngredient[] ingredients = {
            new CraftingItemIngredient(Item.WATER_BOTTLE, 1),
            new CraftingItemIngredient(Item.MUSHROOM_RED, 1),
            new CraftingItemIngredient(Item.FLOWER_RED, 1),
        };
        Optional<Recipe> foundRecipe = Crafting.getRecipeByIngredients(ingredients);

        assertFalse("There should be a recipe.", foundRecipe.isEmpty());
        assertEquals("The found recipe is the correct recipe", recipeOrdered, foundRecipe.get());

        // Cleanup
        Crafting.clearRecipes();
    }
}
