package feature.inventory.ui;

import feature.components.InventoryComponent;
import feature.inventory.Item;

/**
 * This class represents an item dragged from and to an inventory.
 *
 * @param inventoryComponent The inventory the item was dragged from.
 * @param wasHeroInv Whether the inventory is the local hero's inventory.
 * @param slot The slot the item was dragged from.
 * @param item The item that was dragged.
 */
public record ItemDragPayload(
    InventoryComponent inventoryComponent, boolean wasHeroInv, int slot, Item item) {}
