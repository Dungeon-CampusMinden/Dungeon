package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.crafting.CraftingGUI;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogCreationException;
import contrib.hud.elements.InventoryGuiGroup;
import contrib.hud.inventory.InventoryGUI;
import core.Entity;
import core.utils.logging.DungeonLogger;

/**
 * Builds the libGDX-backed crafting dialog.
 */
public final class GdxCraftingDialogBuilder {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(GdxCraftingDialogBuilder.class);

  private GdxCraftingDialogBuilder() {}

  /**
   * Builds a Scene2D crafting dialog from the given context.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the crafting dialog
   */
  public static Group build(DialogContext ctx) {
    Entity entity = ctx.requireEntity(DialogContextKeys.ENTITY);
    Entity craftEntity = ctx.requireEntity(DialogContextKeys.SECONDARY_ENTITY);

    InventoryComponent heroInventory = entity.fetch(InventoryComponent.class).orElse(null);
    InventoryComponent craftInventory = craftEntity.fetch(InventoryComponent.class).orElse(null);

    if (craftInventory == null || heroInventory == null) {
      Entity missingEntity = (craftInventory == null) ? craftEntity : entity;
      LOGGER.error("Entity {} has no InventoryComponent for CraftingGuiDialog", missingEntity);
      throw new DialogCreationException("Missing InventoryComponent for CraftingGuiDialog");
    }

    InventoryGUI inventoryGUI = new InventoryGUI(heroInventory);
    CraftingGUI craftingGUI = new CraftingGUI(craftInventory, heroInventory, ctx.dialogId());

    UIComponent uiComponent =
      entity.fetch(UIComponent.class)
        .orElseThrow(() -> new DialogCreationException("Owner entity has no UIComponent"));

    CraftingGUI.registerCallbacks(uiComponent, craftingGUI);

    return new InventoryGuiGroup(inventoryGUI, craftingGUI);
  }
}
