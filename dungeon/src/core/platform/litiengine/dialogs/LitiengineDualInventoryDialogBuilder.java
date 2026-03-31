package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogCreationException;
import core.Entity;
import core.components.PlayerComponent;
import core.ui.UiNodeHandle;
import core.ui.litiengine.LitiengineUiNodeHandle;
import core.utils.logging.DungeonLogger;

/** Builds the LITIENGINE-backed dual inventory dialog. */
public final class LitiengineDualInventoryDialogBuilder {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(LitiengineDualInventoryDialogBuilder.class);

  private LitiengineDualInventoryDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    Entity entity = ctx.requireEntity(DialogContextKeys.ENTITY);
    Entity otherEntity = ctx.requireEntity(DialogContextKeys.SECONDARY_ENTITY);

    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);
    InventoryComponent otherInventory = otherEntity.fetch(InventoryComponent.class).orElse(null);

    if (inventory == null || otherInventory == null) {
      Entity missingEntity = inventory == null ? entity : otherEntity;
      LOGGER.warn("Entity {} has no InventoryComponent for DualInventoryDialog", missingEntity);
      throw new DialogCreationException("Missing InventoryComponent for DualInventoryDialog");
    }

    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse(defaultTitle(entity));
    String otherTitle =
      ctx.find(DialogContextKeys.SECONDARY_TITLE, String.class).orElse(defaultTitle(otherEntity));

    return new LitiengineUiNodeHandle(
      new LitiengineDualInventoryDialogOverlay(title, inventory, otherTitle, otherInventory));
  }

  private static String defaultTitle(Entity entity) {
    return entity
      .fetch(PlayerComponent.class)
      .map(PlayerComponent::playerName)
      .filter(name -> !name.isBlank())
      .orElseGet(() -> entity.name().isBlank() ? "Inventory" : entity.name());
  }
}
