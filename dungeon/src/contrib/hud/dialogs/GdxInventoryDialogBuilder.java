package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.components.InventoryComponent;
import contrib.hud.elements.InventoryGuiGroup;
import contrib.hud.inventory.InventoryGUI;
import core.Entity;
import core.utils.logging.DungeonLogger;

/**
 * Builds the libGDX-backed inventory dialogs.
 */
public final class GdxInventoryDialogBuilder {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(GdxInventoryDialogBuilder.class);

  private GdxInventoryDialogBuilder() {}

  /**
   * Builds a Scene2D dialog for a single inventory.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the inventory dialog
   */
  public static Group buildSimple(DialogContext ctx) {
    Entity entity = ctx.requireEntity(DialogContextKeys.ENTITY);
    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);

    if (inventory == null) {
      LOGGER.warn("Entity {} has no InventoryComponent for InventoryDialog", entity);
      throw new DialogCreationException("Missing InventoryComponent for InventoryDialog");
    }

    InventoryGUI inventoryGUI = new InventoryGUI(inventory);
    return new InventoryGuiGroup(inventoryGUI);
  }

  /**
   * Builds a Scene2D dialog for two inventories.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the dual-inventory dialog
   */
  public static Group buildDual(DialogContext ctx) {
    Entity entity = ctx.requireEntity(DialogContextKeys.ENTITY);
    Entity otherEntity = ctx.requireEntity(DialogContextKeys.SECONDARY_ENTITY);

    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);
    InventoryComponent otherInventory = otherEntity.fetch(InventoryComponent.class).orElse(null);

    if (inventory == null || otherInventory == null) {
      Entity missingEntity = (inventory == null) ? entity : otherEntity;
      LOGGER.error(
        "Entity {} has no InventoryComponent for DualInventoryDialog", missingEntity);
      throw new DialogCreationException("Missing InventoryComponent for DualInventoryDialog");
    }

    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse(entity.name());
    String otherTitle =
      ctx.find(DialogContextKeys.SECONDARY_TITLE, String.class).orElse(otherEntity.name());

    InventoryGUI inventoryGUI = new InventoryGUI(title, inventory);
    InventoryGUI otherInventoryGUI = new InventoryGUI(otherTitle, otherInventory);
    return new InventoryGuiGroup(inventoryGUI, otherInventoryGUI);
  }
}
