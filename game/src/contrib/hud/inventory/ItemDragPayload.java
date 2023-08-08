package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.utils.components.item.ItemData;

public record ItemDragPayload(InventoryComponent inventoryComponent, int slot, ItemData itemData) {}
