package feature.hud;

import feature.components.InventoryComponent;

/**
 * An interface for dialogs that hold an inventory.
 *
 * @see feature.inventory.ui.InventoryGUI
 */
public interface IInventoryHolder {
  /**
   * Gets the inventory component associated with this holder.
   *
   * @return the inventory component
   */
  InventoryComponent inventoryComponent();
}
