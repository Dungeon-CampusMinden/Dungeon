package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogCreationException;
import core.Entity;
import core.components.PlayerComponent;
import core.ui.UiNodeHandle;
import core.ui.overlay.LitiengineUiNodeHandle;
import core.utils.logging.DungeonLogger;

/** Builds the LITIENGINE-backed crafting dialog. */
public final class LitiengineCraftingDialogBuilder {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(LitiengineCraftingDialogBuilder.class);

  private LitiengineCraftingDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
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

    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse(defaultTitle(entity));
    String craftTitle =
      ctx.find(DialogContextKeys.SECONDARY_TITLE, String.class).orElse(defaultTitle(craftEntity));

    CraftingDialogController controller =
      new CraftingDialogController(heroInventory, craftInventory);
    controller.registerCallbacks(uiComponent);

    return new LitiengineUiNodeHandle(
      new LitiengineCraftingDialogOverlay(title, craftTitle, controller, ctx.dialogId()));
  }

  private static String defaultTitle(Entity entity) {
    return entity
      .fetch(PlayerComponent.class)
      .map(PlayerComponent::playerName)
      .filter(name -> !name.isBlank())
      .orElseGet(() -> entity.name().isBlank() ? "Inventory" : entity.name());
  }
}
