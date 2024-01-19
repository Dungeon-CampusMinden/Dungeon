package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.item.Item;

/**
 * This class represents an item dragged from and to an inventory.
 *
 * @param inventoryComponent The inventory the item was dragged from.
 * @param slot The slot the item was dragged from.
 * @param item The item that was dragged.
 */
public record ItemDragPayload(InventoryComponent inventoryComponent, int slot, Item item) {}
