package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.InventoryDialogSetup;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;
import core.utils.logging.DungeonLogger;

/**
 * A builder for creating dual inventory dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display a dual-inventory dialog overlay,
 * allowing interaction between two entities' inventories.
 *
 * <p>It validates that both entities have InventoryComponents and retrieves custom titles from the
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
   * @throws IllegalArgumentException if required, entities are not present in the context
   */
  public static UiHandle build(DialogContext ctx) {
    InventoryDialogSetup.ResolvedInventory resolvedInventory =
        InventoryDialogSetup.requireInventory(
            ctx, DialogContextKeys.ENTITY, LOGGER, "DualInventoryDialog");
    InventoryDialogSetup.ResolvedInventory resolvedOtherInventory =
        InventoryDialogSetup.requireInventory(
            ctx, DialogContextKeys.SECONDARY_ENTITY, LOGGER, "DualInventoryDialog");
    InventoryComponent inventory = resolvedInventory.inventory();
    InventoryComponent otherInventory = resolvedOtherInventory.inventory();
    String title =
        InventoryDialogSetup.resolveTitle(ctx, DialogContextKeys.TITLE, resolvedInventory);
    String otherTitle =
        InventoryDialogSetup.resolveTitle(
            ctx, DialogContextKeys.SECONDARY_TITLE, resolvedOtherInventory);

    return new OverlayHandle(
        new DualInventoryDialogOverlay(title, inventory, otherTitle, otherInventory));
  }
}
