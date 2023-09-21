package contrib.crafting;

import static org.junit.Assert.*;

import contrib.utils.components.item.ItemData;

import org.junit.Test;

import java.util.Optional;

public class CraftingTest {

    @Test
    public void testFindRecipeWithNoInputs() {
        assertTrue(
                "No Recipe should be found with no ingredients.",
                Crafting.recipeByIngredients(new ItemData[0]).isEmpty());
    }

    @Test
    public void testRecipeFoundUnordered() {
        // Prepare Recipe
        CraftingIngredient[] recipeIngredient = {
            new ItemData(Items.BOTTLE_WATER, 1),
            new ItemData(Items.RESOURCE_MUSHROOM_RED, 1),
            new ItemData(Items.RESOURCE_FLOWER_RED, 1),
        };
        CraftingResult[] recipeResults = {new ItemData(Items.POTION_HEALTH, 1)};
        Recipe recipe = new Recipe(false, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipe);

        // Test

        CraftingIngredient[] ingredients = {
            new ItemData(Items.RESOURCE_FLOWER_RED, 1),
            new ItemData(Items.RESOURCE_MUSHROOM_RED, 1),
            new ItemData(Items.BOTTLE_WATER, 1),
        };

        Optional<Recipe> foundRecipe = Crafting.recipeByIngredients(ingredients);

        assertFalse("There should be a recipe.", foundRecipe.isEmpty());
        assertEquals("The found recipe is the correct recipe", recipe, foundRecipe.get());

        // Cleanup
        Crafting.clearRecipes();
    }

    @Test
    public void testRecipeOrdered_CorrectOrder() {
        // Prepare Recipe
        CraftingIngredient[] recipeIngredient = {
            new ItemData(Items.BOTTLE_WATER, 1),
            new ItemData(Items.RESOURCE_MUSHROOM_RED, 1),
            new ItemData(Items.RESOURCE_FLOWER_RED, 1),
        };
        CraftingResult[] recipeResults = {new ItemData(Items.POTION_HEALTH, 1)};
        Recipe recipe = new Recipe(true, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipe);

        // Test

        CraftingIngredient[] ingredients = {
            new ItemData(Items.BOTTLE_WATER, 1),
            new ItemData(Items.RESOURCE_MUSHROOM_RED, 1),
            new ItemData(Items.RESOURCE_FLOWER_RED, 1),
        };

        Optional<Recipe> foundRecipe = Crafting.recipeByIngredients(ingredients);

        assertFalse("There should be a recipe.", foundRecipe.isEmpty());
        assertEquals("The found recipe is the correct recipe", recipe, foundRecipe.get());

        // Cleanup
        Crafting.clearRecipes();
    }

    @Test
    public void testRecipeOrdered_IncorrectOrder() {
        // Prepare Recipe
        CraftingIngredient[] recipeIngredient = {
            new ItemData(Items.BOTTLE_WATER, 1),
            new ItemData(Items.RESOURCE_MUSHROOM_RED, 1),
            new ItemData(Items.RESOURCE_FLOWER_RED, 1),
        };
        CraftingResult[] recipeResults = {new ItemData(Items.POTION_HEALTH, 1)};
        Recipe recipe = new Recipe(true, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipe);

        // Test

        CraftingIngredient[] ingredients = {
            new ItemData(Items.RESOURCE_MUSHROOM_RED, 1),
            new ItemData(Items.RESOURCE_FLOWER_RED, 1),
            new ItemData(Items.BOTTLE_WATER, 1),
        };

        Optional<Recipe> foundRecipe = Crafting.recipeByIngredients(ingredients);

        assertTrue("There should be no recipe.", foundRecipe.isEmpty());

        // Cleanup
        Crafting.clearRecipes();
    }

    @Test
    public void testPrioritizeOrderedRecipes() {
        // Prepare Recipe
        CraftingIngredient[] recipeIngredient = {
            new ItemData(Items.BOTTLE_WATER, 1),
            new ItemData(Items.RESOURCE_MUSHROOM_RED, 1),
            new ItemData(Items.RESOURCE_FLOWER_RED, 1),
        };
        CraftingResult[] recipeResults = {new ItemData(Items.POTION_HEALTH, 1)};
        Recipe recipeOrdered = new Recipe(true, recipeIngredient, recipeResults);
        Recipe recipeUnordered = new Recipe(false, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipeUnordered);
        Crafting.addRecipe(recipeOrdered);

        // Test
        CraftingIngredient[] ingredients = {
            new ItemData(Items.BOTTLE_WATER, 1),
            new ItemData(Items.RESOURCE_MUSHROOM_RED, 1),
            new ItemData(Items.RESOURCE_FLOWER_RED, 1),
        };
        Optional<Recipe> foundRecipe = Crafting.recipeByIngredients(ingredients);

        assertFalse("There should be a recipe.", foundRecipe.isEmpty());
        assertEquals("The found recipe is the correct recipe", recipeOrdered, foundRecipe.get());

        // Cleanup
        Crafting.clearRecipes();
    }
}
