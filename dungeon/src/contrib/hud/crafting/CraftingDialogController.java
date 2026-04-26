package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.crafting.Crafting;
import contrib.crafting.CraftingType;
import contrib.crafting.Recipe;
import contrib.item.Item;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * A controller for managing crafting dialog logic and inventory transfers.
 *
 * <p>This record manages the interaction between target inventory (player) and crafting inventory
 * during a crafting session.
 *
 * <p>It provides methods for transferring items between inventories, resolving recipes, and
 * executing craft actions.
 *
 * @param targetInventory the inventory that receives crafted items and provides source items
 * @param craftingInventory the inventory used as crafting input
 */
public record CraftingDialogController(
    InventoryComponent targetInventory, InventoryComponent craftingInventory) {
  /** Standard crafting callback. */
  public static final String CALLBACK_CRAFT = "craft";

  /** Standard cancel callback. */
  public static final String CALLBACK_CANCEL = "cancel";

  /**
   * Creates a new controller for one crafting dialog session.
   *
   * @param targetInventory the inventory that receives crafted items and provides source items
   * @param craftingInventory the inventory used as crafting input
   */
  public CraftingDialogController {}

  /**
   * Gets the target inventory's item slots.
   *
   * @return array of items in the target inventory
   */
  public Item[] targetSlots() {
    return targetInventory.items();
  }

  /**
   * Gets the crafting inventory's item slots.
   *
   * @return array of items in the crafting inventory
   */
  public Item[] craftingSlots() {
    return craftingInventory.items();
  }

  /**
   * Returns the current crafting input payload that should be sent with the craft callback.
   *
   * @return copy of the current crafting inventory contents
   */
  public Item[] craftingPayload() {
    return craftingInventory.items();
  }

  /**
   * Applies a crafting payload received from a callback.
   *
   * @param items crafting input payload
   */
  public void applyCraftingPayload(Item[] items) {
    if (items == null) {
      craftingInventory.clear();
      return;
    }

    craftingInventory.setItems(items);
  }

   /**
    * Resolves the currently matching recipe for the current crafting inventory contents.
    *
    * @return current recipe if one matches
    */
   public Optional<Recipe> currentRecipe() {
     Item[] ingredients =
         Arrays.stream(craftingInventory.items())
             .filter(Objects::nonNull)
             .toArray(Item[]::new);

     if (ingredients.length == 0) {
       return Optional.empty();
     }

     return Crafting.recipeByIngredients(ingredients);
   }

  /**
   * Transfers the given item from the specified side to the opposite side.
   *
   * @param sourceSide the source side
   * @param item the item to move
   */
  void transferByItem(CraftingInventorySide sourceSide, Item item) {
    if (item == null) {
      return;
    }

    switch (sourceSide) {
      case TARGET -> targetInventory.transfer(item, craftingInventory);
      case CRAFTING -> craftingInventory.transfer(item, targetInventory);
    }
  }

  /**
   * Transfers the item at the given slot from the specified side to the opposite side.
   *
   * @param sourceSide the source side
   * @param slotIndex the slot index
   */
  public void transferBySlot(CraftingInventorySide sourceSide, int slotIndex) {
    InventoryComponent source =
        sourceSide == CraftingInventorySide.TARGET ? targetInventory : craftingInventory;
    Item item = source.get(slotIndex).orElse(null);
    transferByItem(sourceSide, item);
  }

  /**
   * Transfers the item from a concrete source slot to a concrete target slot on the opposite side.
   *
   * <p>This method is intentionally strict:
   *
   * <ul>
   *   <li>source and target side must differ
   *   <li>the source slot must contain an item
   *   <li>the target slot must be empty
   * </ul>
   *
   * <p>This provides the backend-neutral semantic foundation for exact slot drops in concrete UI
   * backends such as LITIENGINE.
   *
   * @param sourceSide source inventory side
   * @param sourceSlotIndex source slot index
   * @param targetSide target inventory side
   * @param targetSlotIndex target slot index
   */
  public void transferBySlotToSlot(
      CraftingInventorySide sourceSide,
      int sourceSlotIndex,
      CraftingInventorySide targetSide,
      int targetSlotIndex) {
    if (sourceSide == null || targetSide == null) {
      return;
    }

    if (sourceSide == targetSide) {
      return;
    }

    InventoryComponent source = inventoryOf(sourceSide);
    InventoryComponent target = inventoryOf(targetSide);

    Item item = source.get(sourceSlotIndex).orElse(null);
    if (item == null) {
      return;
    }

    if (target.get(targetSlotIndex).isPresent()) {
      return;
    }

    if (source.remove(sourceSlotIndex).isEmpty()) {
      return;
    }

    target.set(targetSlotIndex, item);
  }

   /** Executes the craft action on the current crafting inventory. */
   public void craft() {
     Optional<Recipe> recipe = currentRecipe();
     if (recipe.isEmpty()) {
       return;
     }

     Arrays.stream(recipe.get().results())
         .filter(result -> result.resultType() == CraftingType.ITEM && result instanceof Item)
         .map(Item.class::cast)
         .forEach(targetInventory::add);

     craftingInventory.clear();
   }

   /** Cancels the current crafting attempt and returns all inputs to the target inventory. */
   public void cancel() {
     craftingInventory.transferAll(targetInventory);
   }

  private InventoryComponent inventoryOf(CraftingInventorySide side) {
    return side == CraftingInventorySide.TARGET ? targetInventory : craftingInventory;
  }
}
