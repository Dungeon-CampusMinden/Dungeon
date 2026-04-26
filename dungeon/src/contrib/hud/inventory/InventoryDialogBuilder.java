package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogContextFactory;
import contrib.hud.dialogs.DialogCreationException;
import core.Entity;
import core.components.PlayerComponent;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;
import core.utils.logging.DungeonLogger;

/**
 * A builder for creating inventory dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display an inventory dialog overlay.
 *
 * <p>It validates that the entity has an InventoryComponent and retrieves a custom title from
 * the dialog context or generates a default based on the entity's player name or entity name.
 */
public final class InventoryDialogBuilder {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(InventoryDialogBuilder.class);

  private InventoryDialogBuilder() {}

  /**
   * Builds a UI node handle for an inventory dialog overlay.
   *
   * <p>This method requires the dialog context to contain an entity with an InventoryComponent.
   *
   * <p>It retrieves an optional custom title from the context or generates a default title based
   * on the entity's player name or entity name. Item usage is enabled if the entity is a player.
   *
   * @param ctx the dialog context containing the entity and optional configuration
   * @return a UI node handle wrapping the created inventory dialog overlay
   * @throws DialogCreationException if the entity lacks an InventoryComponent
   * @throws IllegalArgumentException if the entity is not present in the context
   */
  public static UiHandle build(DialogContext ctx) {
    Entity entity = ctx.requireEntity(DialogContextKeys.ENTITY);
    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);

    if (inventory == null) {
      LOGGER.warn("Entity {} has no InventoryComponent for InventoryDialog", entity);
      throw new DialogCreationException("Missing InventoryComponent for InventoryDialog");
    }

    String title = DialogContextFactory.inventoryTitle(ctx, DialogContextKeys.TITLE, entity);
    boolean allowUseItems = entity.isPresent(PlayerComponent.class);

    return new OverlayHandle(
      new InventoryDialogOverlay(title, entity, inventory, allowUseItems));
  }
}
