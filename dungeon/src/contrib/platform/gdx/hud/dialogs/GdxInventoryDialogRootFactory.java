package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.components.InventoryComponent;
import contrib.hud.elements.InventoryGuiGroup;
import contrib.hud.inventory.InventoryGUI;

/**
 * Small libGDX-only factory for legacy inventory dialog roots.
 *
 * <p>This keeps the remaining direct {@link InventoryGUI}/{@link InventoryGuiGroup}
 * construction out of the active dialog builder path, so later migration steps can
 * remove or replace the old Scene2D inventory implementation in one place.
 */
final class GdxInventoryDialogRootFactory {

  private GdxInventoryDialogRootFactory() {}

  /**
   * Creates the legacy Scene2D root for a single inventory dialog.
   *
   * @param inventory the inventory to display
   * @return Scene2D root group
   */
  static Group createSimple(InventoryComponent inventory) {
    return new InventoryGuiGroup(new InventoryGUI(inventory));
  }

  /**
   * Creates the legacy Scene2D root for a dual-inventory dialog.
   *
   * @param title title of the first inventory
   * @param inventory first inventory
   * @param otherTitle title of the second inventory
   * @param otherInventory second inventory
   * @return Scene2D root group
   */
  static Group createDual(
    String title,
    InventoryComponent inventory,
    String otherTitle,
    InventoryComponent otherInventory) {
    InventoryGUI inventoryGUI = new InventoryGUI(title, inventory);
    InventoryGUI otherInventoryGUI = new InventoryGUI(otherTitle, otherInventory);
    return new InventoryGuiGroup(inventoryGUI, otherInventoryGUI);
  }
}
