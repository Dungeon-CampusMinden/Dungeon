package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.crafting.Recipe;
import contrib.hud.UIUtils;
import contrib.item.Item;
import core.utils.logging.DungeonLogger;
import java.util.Optional;

/**
 * A controller for managing crafting dialog logic and inventory transfers.
 *
 * <p>This record manages the interaction between target inventory (player) and crafting inventory
 * during a crafting session.
 *
 * <p>It provides methods for transferring items between inventories,
 * resolving recipes, executing craft actions, and managing callbacks for crafting operations.
 *
 * @param targetInventory   the inventory that receives crafted items and provides source items
 * @param craftingInventory the inventory used as crafting input
 */
public record CraftingDialogController(InventoryComponent targetInventory, InventoryComponent craftingInventory) {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(CraftingDialogController.class);

  /** Standard crafting callback. */
  public static final String CALLBACK_CRAFT = "craft";

  /** Standard cancel callback. */
  public static final String CALLBACK_CANCEL = "cancel";

  /**
   * Creates a new controller for one crafting dialog session.
   *
   * @param targetInventory   the inventory that receives crafted items and provides source items
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
    return CraftingDialogLogic.currentRecipe(craftingInventory);
  }

  /**
   * Transfers the given item from the specified side to the opposite side.
   *
   * @param sourceSide the source side
   * @param item       the item to move
   */
  public void transferByItem(InventorySide sourceSide, Item item) {
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
   * @param slotIndex  the slot index
   */
  public void transferBySlot(InventorySide sourceSide, int slotIndex) {
    InventoryComponent source =
      sourceSide == InventorySide.TARGET ? targetInventory : craftingInventory;
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
   * @param sourceSide      source inventory side
   * @param sourceSlotIndex source slot index
   * @param targetSide      target inventory side
   * @param targetSlotIndex target slot index
   */
  public void transferBySlotToSlot(
    InventorySide sourceSide,
    int sourceSlotIndex,
    InventorySide targetSide,
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
      _ -> {
        cancel();
        UIUtils.closeDialog(uiComponent);
      });

    uiComponent.onClose(_ -> cancel());
  }

  /**
   * Specifies the inventory side involved in a crafting operation.
   *
   * <p>This enum is used to differentiate between the target inventory and
   * the crafting inventory in operations such as transferring items or
   * executing crafting actions.
   *
   * <p>The two sides are:
   * <ul>
   *   <li>TARGET: Represents the target inventory which receives crafted items
   *   and provides source items.</li>
   *   <li>CRAFTING: Represents the crafting inventory which holds the items
   *   used as crafting input.</li>
   * </ul>
   *
   * <p>The {@code InventorySide} enum is central to the behavior of crafting
   * dialogs and operations, enabling backend-neutral manipulation of inventories.
   */
  public enum InventorySide {
    /** Represents the target inventory. */
    TARGET,
    /** Represents the crafting inventory. */
    CRAFTING
  }
}
