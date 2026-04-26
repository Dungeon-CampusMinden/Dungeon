package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextFactory;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogCreationException;
import core.Entity;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;
import core.utils.logging.DungeonLogger;

/**
 * A builder for creating dual inventory dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display a dual-inventory dialog overlay,
 * allowing interaction between two entities' inventories.
 *
 * <p>>It validates that both entities have InventoryComponents and retrieves custom titles from the
 * dialog context or generates defaults.
 */
public final class DualInventoryDialogBuilder {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(DualInventoryDialogBuilder.class);

  private DualInventoryDialogBuilder() {}

  /**
   * Builds a UI node handle for a dual-inventory dialog overlay.
   *
   * <p>This method requires the dialog context to contain both a primary entity and a secondary
   * entity, each with an InventoryComponent.
   *
   * <p>It retrieves optional custom titles from the context or generates default titles based on
   * player names or entity names.
   *
   * @param ctx the dialog context containing the entities and optional configuration
   * @return a UI node handle wrapping the created dual-inventory dialog overlay
   * @throws DialogCreationException if either entity lacks an InventoryComponent
   * @throws IllegalArgumentException if required, entities are not present in the context
   */
  public static UiHandle build(DialogContext ctx) {
    Entity entity = ctx.requireEntity(DialogContextKeys.ENTITY);
    Entity otherEntity = ctx.requireEntity(DialogContextKeys.SECONDARY_ENTITY);

    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);
    InventoryComponent otherInventory = otherEntity.fetch(InventoryComponent.class).orElse(null);

    if (inventory == null || otherInventory == null) {
      Entity missingEntity = inventory == null ? entity : otherEntity;
      LOGGER.warn("Entity {} has no InventoryComponent for DualInventoryDialog", missingEntity);
      throw new DialogCreationException("Missing InventoryComponent for DualInventoryDialog");
    }

    String title = DialogContextFactory.inventoryTitle(ctx, DialogContextKeys.TITLE, entity);
    String otherTitle =
        DialogContextFactory.inventoryTitle(ctx, DialogContextKeys.SECONDARY_TITLE, otherEntity);

    return new OverlayHandle(
        new DualInventoryDialogOverlay(title, inventory, otherTitle, otherInventory));
  }
}
