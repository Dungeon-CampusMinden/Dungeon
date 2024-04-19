package contrib.crafting;

import static org.junit.Assert.*;

import contrib.item.HealthPotionType;
import contrib.item.Item;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.item.concreteItem.ItemResourceMushroomRed;
import java.util.Optional;
import org.junit.Test;

/** Tests for the {@link Crafting} class. */
public class CraftingTest {

  /** WTF? . */
  @Test
  public void testFindRecipeWithNoInputs() {
    assertTrue(
        "No Recipe should be found with no ingredients.",
        Crafting.recipeByIngredients(new Item[0]).isEmpty());
  }

  /** WTF? . */
  @Test
  public void testRecipeFoundUnordered() {
    // Prepare Recipe
    CraftingIngredient[] recipeIngredient = {
      new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceMushroomRed(),
    };
    CraftingResult[] recipeResults = {new ItemPotionHealth(HealthPotionType.NORMAL)};
    Recipe recipe = new Recipe(false, recipeIngredient, recipeResults);
    Crafting.addRecipe(recipe);

    // Test

    CraftingIngredient[] ingredients = {
      new ItemResourceMushroomRed(), new ItemResourceMushroomRed(), new ItemPotionWater()
    };

    Optional<Recipe> foundRecipe = Crafting.recipeByIngredients(ingredients);

    assertFalse("There should be a recipe.", foundRecipe.isEmpty());
    assertEquals("The found recipe is the correct recipe", recipe, foundRecipe.get());

    // Cleanup
    Crafting.clearRecipes();
  }

  /** WTF? . */
  @Test
  public void testRecipeOrdered_CorrectOrder() {
    // Prepare Recipe
    CraftingIngredient[] recipeIngredient = {
      new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceMushroomRed(),
    };
    CraftingResult[] recipeResults = {new ItemPotionHealth(HealthPotionType.NORMAL)};
    Recipe recipe = new Recipe(true, recipeIngredient, recipeResults);
    Crafting.addRecipe(recipe);

    // Test

    CraftingIngredient[] ingredients = {
      new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceMushroomRed(),
    };

    Optional<Recipe> foundRecipe = Crafting.recipeByIngredients(ingredients);

    assertFalse("There should be a recipe.", foundRecipe.isEmpty());
    assertEquals("The found recipe is the correct recipe", recipe, foundRecipe.get());

    // Cleanup
    Crafting.clearRecipes();
  }

  /** WTF? . */
  @Test
  public void testRecipeOrdered_IncorrectOrder() {
    // Prepare Recipe
    CraftingIngredient[] recipeIngredient = {
      new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceMushroomRed(),
    };
    CraftingResult[] recipeResults = {new ItemPotionHealth(HealthPotionType.NORMAL)};
    Recipe recipe = new Recipe(true, recipeIngredient, recipeResults);
    Crafting.addRecipe(recipe);

    // Test

    CraftingIngredient[] ingredients = {
      new ItemResourceMushroomRed(), new ItemResourceMushroomRed(), new ItemPotionWater(),
    };

    Optional<Recipe> foundRecipe = Crafting.recipeByIngredients(ingredients);

    assertTrue("There should be no recipe.", foundRecipe.isEmpty());

    // Cleanup
    Crafting.clearRecipes();
  }

  /** WTF? . */
  @Test
  public void testPrioritizeOrderedRecipes() {
    // Prepare Recipe
    CraftingIngredient[] recipeIngredient = {
      new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceMushroomRed(),
    };
    CraftingResult[] recipeResults = {new ItemPotionHealth(HealthPotionType.NORMAL)};
    Recipe recipeOrdered = new Recipe(true, recipeIngredient, recipeResults);
    Recipe recipeUnordered = new Recipe(false, recipeIngredient, recipeResults);
    Crafting.addRecipe(recipeUnordered);
    Crafting.addRecipe(recipeOrdered);

    // Test
    CraftingIngredient[] ingredients = {
      new ItemPotionWater(), new ItemResourceMushroomRed(), new ItemResourceMushroomRed(),
    };
    Optional<Recipe> foundRecipe = Crafting.recipeByIngredients(ingredients);

    assertFalse("There should be a recipe.", foundRecipe.isEmpty());
    assertEquals("The found recipe is the correct recipe", recipeOrdered, foundRecipe.get());

    // Cleanup
    Crafting.clearRecipes();
  }
}
