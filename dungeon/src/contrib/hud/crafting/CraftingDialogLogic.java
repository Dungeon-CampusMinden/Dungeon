package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.crafting.Crafting;
import contrib.crafting.CraftingResult;
import contrib.crafting.CraftingType;
import contrib.crafting.Recipe;
import contrib.item.Item;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Shared backend-neutral crafting dialog logic.
 *
 * <p>This class contains the semantic behavior used by different crafting UIs, independent of the
 * concrete rendering toolkit (libGDX Scene2D or LITIENGINE overlay).
 */
public final class CraftingDialogLogic {

  private CraftingDialogLogic() {}

  /**
   * Resolves the currently matching recipe for the non-empty items in the crafting inventory.
   *
   * @param craftingInventory the inventory that contains the crafting inputs
   * @return the matching recipe, if any
   */
  public static Optional<Recipe> currentRecipe(InventoryComponent craftingInventory) {
    Item[] ingredients =
      Arrays.stream(craftingInventory.items()).filter(Objects::nonNull).toArray(Item[]::new);

    if (ingredients.length == 0) {
      return Optional.empty();
    }

    return Crafting.recipeByIngredients(ingredients);
  }

  /**
   * Crafts the currently matching recipe and moves item results into the target inventory.
   *
   * <p>If no recipe matches, nothing happens.
   *
   * @param craftingInventory the inventory containing the crafting inputs
   * @param targetInventory the inventory receiving crafting results
   */
  public static void craft(
    InventoryComponent craftingInventory, InventoryComponent targetInventory) {
    Optional<Recipe> recipe = currentRecipe(craftingInventory);
    if (recipe.isEmpty()) {
      return;
    }

    Arrays.stream(recipe.get().results())
      .filter(result -> result.resultType() == CraftingType.ITEM && result instanceof Item)
      .map(Item.class::cast)
      .forEach(targetInventory::add);

    craftingInventory.clear();
  }

  /**
   * Cancels the current crafting attempt and transfers all inserted items back to the target
   * inventory.
   *
   * @param craftingInventory the inventory containing the crafting inputs
   * @param targetInventory the inventory receiving the returned items
   */
  public static void cancel(
    InventoryComponent craftingInventory, InventoryComponent targetInventory) {
    craftingInventory.transferAll(targetInventory);
  }

  /**
   * Converts a crafting result into a user-facing label.
   *
   * @param result the result to label
   * @return display name for item results, otherwise the result type name
   */
  public static String resultLabel(CraftingResult result) {
    if (result instanceof Item item) {
      return item.displayName();
    }

    return result.resultType().name();
  }
}
