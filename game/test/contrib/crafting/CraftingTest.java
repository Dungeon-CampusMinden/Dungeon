package contrib.crafting;

import static org.junit.Assert.*;

import contrib.item.Item;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.item.concreteItem.ItemResourceFlowerRed;
import contrib.item.concreteItem.ItemResourceMushroomRed;

import org.junit.Test;

import java.util.Optional;

public class CraftingTest {

    @Test
    public void testFindRecipeWithNoInputs() {
        assertTrue(
                "No Recipe should be found with no ingredients.",
                Crafting.recipeByIngredients(new Item[0]).isEmpty());
    }

    @Test
    public void testRecipeFoundUnordered() {
        // Prepare Recipe
        CraftingIngredient[] recipeIngredient = {
            new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceFlowerRed(),
        };
        CraftingResult[] recipeResults = {new ItemPotionHealth()};
        Recipe recipe = new Recipe(false, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipe);

        // Test

        CraftingIngredient[] ingredients = {
            new ItemResourceFlowerRed(), new ItemResourceMushroomRed(), new ItemPotionWater()
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
            new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceFlowerRed(),
        };
        CraftingResult[] recipeResults = {new ItemPotionHealth()};
        Recipe recipe = new Recipe(true, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipe);

        // Test

        CraftingIngredient[] ingredients = {
            new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceFlowerRed(),
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
            new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceFlowerRed(),
        };
        CraftingResult[] recipeResults = {new ItemPotionHealth()};
        Recipe recipe = new Recipe(true, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipe);

        // Test

        CraftingIngredient[] ingredients = {
            new ItemResourceMushroomRed(), new ItemResourceFlowerRed(), new ItemPotionWater(),
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
            new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceFlowerRed(),
        };
        CraftingResult[] recipeResults = {new ItemPotionHealth()};
        Recipe recipeOrdered = new Recipe(true, recipeIngredient, recipeResults);
        Recipe recipeUnordered = new Recipe(false, recipeIngredient, recipeResults);
        Crafting.addRecipe(recipeUnordered);
        Crafting.addRecipe(recipeOrdered);

        // Test
        CraftingIngredient[] ingredients = {
            new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceFlowerRed(),
        };
        Optional<Recipe> foundRecipe = Crafting.recipeByIngredients(ingredients);

        assertFalse("There should be a recipe.", foundRecipe.isEmpty());
        assertEquals("The found recipe is the correct recipe", recipeOrdered, foundRecipe.get());

        // Cleanup
        Crafting.clearRecipes();
    }
}
