package contrib.crafting;

import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.item.Item;
import core.utils.logging.DungeonLogger;
import java.util.Arrays;
import java.util.Optional;

/**
 * Shared controller for crafting dialogs across different UI backends.
 *
 * <p>This class owns the backend-neutral crafting interaction logic: item transfer between target
 * and crafting inventory, recipe lookup, result projection, callback payload handling, and the
 * standard craft/cancel callback registration.
 *
 * <p>Rendering remains backend-specific.
 */
public record CraftingDialogController(InventoryComponent targetInventory, InventoryComponent craftingInventory) {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(CraftingDialogController.class);

  public static final String CALLBACK_CRAFT = "craft";
  public static final String CALLBACK_CANCEL = "cancel";

  /**
   * Creates a new controller for one crafting dialog session.
   *
   * @param targetInventory   the inventory that receives crafted items and provides source items
   * @param craftingInventory the inventory used as crafting input
   */
  public CraftingDialogController {}

  public Item[] targetSlots() {
    return targetInventory.items();
  }

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
    return CraftingDialogLogic.currentRecipe(craftingInventory);
  }

  /**
   * Returns the currently craftable item results.
   *
   * <p>Non-item crafting results are intentionally ignored here because both the current GDX and
   * LITIENGINE crafting UIs only preview item results visually.
   *
   * @return current craftable item results
   */
  public Item[] resultItems() {
    return currentRecipe()
      .map(
        recipe ->
          Arrays.stream(recipe.results())
            .filter(
              result ->
                result.resultType() == CraftingType.ITEM && result instanceof Item)
            .map(Item.class::cast)
            .toArray(Item[]::new))
      .orElse(new Item[0]);
  }

  /**
   * Transfers the given item from the specified side to the opposite side.
   *
   * @param sourceSide the source side
   * @param item       the item to move
   * @return true if the transfer succeeded
   */
  public boolean transferByItem(InventorySide sourceSide, Item item) {
    if (item == null) {
      return false;
    }

    return switch (sourceSide) {
      case TARGET -> targetInventory.transfer(item, craftingInventory);
      case CRAFTING -> craftingInventory.transfer(item, targetInventory);
    };
  }

  /**
   * Transfers the item at the given slot from the specified side to the opposite side.
   *
   * @param sourceSide the source side
   * @param slotIndex  the slot index
   * @return true if the transfer succeeded
   */
  public boolean transferBySlot(InventorySide sourceSide, int slotIndex) {
    InventoryComponent source =
      sourceSide == InventorySide.TARGET ? targetInventory : craftingInventory;
    Item item = source.get(slotIndex).orElse(null);
    return transferByItem(sourceSide, item);
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
   * @return true if the transfer succeeded
   */
  public boolean transferBySlotToSlot(
    InventorySide sourceSide,
    int sourceSlotIndex,
    InventorySide targetSide,
    int targetSlotIndex) {
    if (sourceSide == null || targetSide == null) {
      return false;
    }

    if (sourceSide == targetSide) {
      return false;
    }

    InventoryComponent source = inventoryOf(sourceSide);
    InventoryComponent target = inventoryOf(targetSide);

    Item item = source.get(sourceSlotIndex).orElse(null);
    if (item == null) {
      return false;
    }

    if (target.get(targetSlotIndex).isPresent()) {
      return false;
    }

    if (source.remove(sourceSlotIndex).isEmpty()) {
      return false;
    }

    target.set(targetSlotIndex, item);
    return true;
  }

  private InventoryComponent inventoryOf(InventorySide side) {
    return side == InventorySide.TARGET ? targetInventory : craftingInventory;
  }

  /**
   * Executes the craft action on the current crafting inventory.
   */
  public void craft() {
    CraftingDialogLogic.craft(craftingInventory, targetInventory);
  }

  /**
   * Cancels the current crafting attempt and returns all inputs to the target inventory.
   */
  public void cancel() {
    CraftingDialogLogic.cancel(craftingInventory, targetInventory);
  }

  /**
   * Registers the standard craft/cancel callbacks for this dialog controller.
   *
   * @param uiComponent the UI component that owns the dialog
   */
  public void registerCallbacks(UIComponent uiComponent) {
    uiComponent.registerCallback(
      CALLBACK_CRAFT,
      data -> {
        if (data instanceof Item[] items) {
          applyCraftingPayload(items);
        } else {
          LOGGER.warn("Invalid data for crafting callback: expected Item[], got {}", data);
        }

        craft();
        UIUtils.closeDialog(uiComponent);
      });

    uiComponent.registerCallback(
      CALLBACK_CANCEL,
      data -> {
        cancel();
        UIUtils.closeDialog(uiComponent);
      });

    uiComponent.onClose(ui -> cancel());
  }

  /**
   * Dialog-local source side for transfers.
   */
  public enum InventorySide {
    TARGET,
    CRAFTING
  }
}
