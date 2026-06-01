package contrib.hud;

import contrib.components.InventoryComponent;

/**
 * An interface for dialogs that hold an inventory.
 *
 * @see contrib.hud.inventory.InventoryGUI
 */
public interface IInventoryHolder {
  /**
   * Gets the inventory component associated with this holder.
   *
   * @return the inventory component
   */
  InventoryComponent inventoryComponent();
}
