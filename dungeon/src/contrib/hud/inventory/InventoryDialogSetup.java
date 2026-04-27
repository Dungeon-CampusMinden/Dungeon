package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextFactory;
import contrib.hud.dialogs.DialogCreationException;
import core.Entity;
import core.utils.logging.DungeonLogger;

/**
 * Utility class for setting up and validating an inventory dialog in the context of a game
 * or application.
 *
 * <p>Provides helper methods to resolve entities, validate inventory components,
 * and determine inventory-related properties required for dialog creation.
 */
public final class InventoryDialogSetup {

  private InventoryDialogSetup() {}

  /**
   * Resolves an entity from the dialog context and validates that it has an inventory.
   *
   * @param ctx the dialog context containing the entity reference
   * @param entityKey the context key storing the entity reference
   * @param logger the logger used for warning output
   * @param dialogName the dialog name used in log and exception messages
   * @return the resolved entity and inventory
   */
  public static ResolvedInventory requireInventory(
    DialogContext ctx, String entityKey, DungeonLogger logger, String dialogName) {
    Entity entity = ctx.requireEntity(entityKey);
    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);

    if (inventory == null) {
      logger.warn("Entity {} has no InventoryComponent for {}", entity, dialogName);
      throw new DialogCreationException("Missing InventoryComponent for " + dialogName);
    }

    return new ResolvedInventory(entity, inventory);
  }

  /**
   * Resolves an inventory-style title from the context or the resolved entity.
   *
   * @param ctx the dialog context containing an optional title override
   * @param titleKey the context key storing the title override
   * @param resolvedInventory the resolved entity and inventory
   * @return the configured or default inventory title
   */
  public static String resolveTitle(
      DialogContext ctx, String titleKey, ResolvedInventory resolvedInventory) {
    return DialogContextFactory.inventoryTitle(ctx, titleKey, resolvedInventory.entity());
  }

  /** Resolved entity and inventory pair for inventory dialog setup. */
  public record ResolvedInventory(Entity entity, InventoryComponent inventory) {}
}
