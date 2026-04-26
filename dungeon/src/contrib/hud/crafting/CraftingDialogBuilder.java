package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogContextHelper;
import contrib.hud.dialogs.DialogCreationException;
import contrib.item.Item;
import core.Entity;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;
import core.utils.logging.DungeonLogger;

/**
 * A builder for creating crafting dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display a crafting dialog overlay.
 *
 * <p>It validates that both the player entity and crafting entity have InventoryComponents,
 * registers crafting callbacks with the UI component, and retrieves custom titles from
 * the dialog context or generates defaults based on entity names.
 */
public final class CraftingDialogBuilder {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(CraftingDialogBuilder.class);

  private CraftingDialogBuilder() {}

  /**
   * Builds a UI node handle for a crafting dialog overlay.
   *
   * <p>This method requires the dialog context to contain both a primary entity (player) and a
   * secondary entity (crafting station). Both entities must have InventoryComponents.
   *
   * <p>The player entity must also have a UIComponent to register crafting callbacks.
   *
   * <p>It retrieves optional custom titles from the context or generates default titles based on
   * player names or entity names.
   *
   * @param ctx the dialog context containing the player entity, crafting entity, and optional configuration
   * @return a UI node handle wrapping the created crafting dialog overlay
   * @throws DialogCreationException if either entity lacks an InventoryComponent, or if the player
   *         entity lacks a UIComponent
   * @throws IllegalArgumentException if required, entities are not present in the context
   */
  public static UiHandle build(DialogContext ctx) {
    Entity entity = ctx.requireEntity(DialogContextKeys.ENTITY);
    Entity craftEntity = ctx.requireEntity(DialogContextKeys.SECONDARY_ENTITY);

    InventoryComponent heroInventory = entity.fetch(InventoryComponent.class).orElse(null);
    InventoryComponent craftInventory = craftEntity.fetch(InventoryComponent.class).orElse(null);

    if (heroInventory == null || craftInventory == null) {
      Entity missingEntity = heroInventory == null ? entity : craftEntity;
      LOGGER.warn("Entity {} has no InventoryComponent for CraftingGuiDialog", missingEntity);
      throw new DialogCreationException("Missing InventoryComponent for CraftingGuiDialog");
    }

    UIComponent uiComponent =
      entity.fetch(UIComponent.class)
        .orElseThrow(() -> new DialogCreationException("Owner entity has no UIComponent"));

    String title = DialogContextHelper.inventoryTitle(ctx, DialogContextKeys.TITLE, entity);
    String craftTitle =
      DialogContextHelper.inventoryTitle(ctx, DialogContextKeys.SECONDARY_TITLE, craftEntity);

    CraftingDialogController controller =
      new CraftingDialogController(heroInventory, craftInventory);
    registerCallbacks(uiComponent, controller);

    return new OverlayHandle(
      new CraftingDialogOverlay(title, craftTitle, controller, ctx.dialogId()));
  }

  private static void registerCallbacks(
    UIComponent uiComponent, CraftingDialogController controller) {
    uiComponent.registerCallback(
      CraftingDialogController.CALLBACK_CRAFT,
      data -> {
        if (data instanceof Item[] items) {
          controller.applyCraftingPayload(items);
        } else {
          LOGGER.warn("Invalid data for crafting callback: expected Item[], got {}", data);
        }

        controller.craft();
        UIUtils.closeDialog(uiComponent);
      });

    uiComponent.registerCallback(
      CraftingDialogController.CALLBACK_CANCEL,
      _ -> {
        controller.cancel();
        UIUtils.closeDialog(uiComponent);
      });

    uiComponent.onClose(_ -> controller.cancel());
  }
}
