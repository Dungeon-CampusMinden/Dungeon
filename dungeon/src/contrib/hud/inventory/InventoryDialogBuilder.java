package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogCreationException;
import core.Entity;
import core.components.PlayerComponent;
import core.ui.UiNodeHandle;
import core.ui.overlay.LitiengineUiNodeHandle;
import core.utils.logging.DungeonLogger;

/** Builds the LITIENGINE-backed inventory dialog. */
public final class InventoryDialogBuilder {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(InventoryDialogBuilder.class);

  private InventoryDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    Entity entity = ctx.requireEntity(DialogContextKeys.ENTITY);
    InventoryComponent inventory = entity.fetch(InventoryComponent.class).orElse(null);

    if (inventory == null) {
      LOGGER.warn("Entity {} has no InventoryComponent for InventoryDialog", entity);
      throw new DialogCreationException("Missing InventoryComponent for InventoryDialog");
    }

    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse(defaultTitle(entity));
    boolean allowUseItems = entity.isPresent(PlayerComponent.class);

    return new LitiengineUiNodeHandle(
      new InventoryDialogOverlay(title, entity, inventory, allowUseItems));
  }

  private static String defaultTitle(Entity entity) {
    return entity
      .fetch(PlayerComponent.class)
      .map(PlayerComponent::playerName)
      .filter(name -> !name.isBlank())
      .orElseGet(() -> entity.name().isBlank() ? "Inventory" : entity.name());
  }
}
