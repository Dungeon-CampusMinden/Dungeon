package contrib.hud.dialogs;

import contrib.hud.dialogs.showimage.ShowImageText;
import contrib.hud.dialogs.showimage.TransitionSpeed;
import core.Entity;
import core.components.PlayerComponent;

/** Shared helpers for common dialog context construction and default values. */
public final class DialogContextHelper {

  private static final String DEFAULT_INVENTORY_TITLE = "Inventory";

  private DialogContextHelper() {}

  /**
   * Builds the common context payload for image dialogs.
   *
   * @param imagePath the path to the image to display
   * @param speed the transition speed for showing the image
   * @param maxSize the maximum size factor of the image relative to the screen
   * @param textConfig optional text configuration rendered on top of the image
   * @param ownerEntityId the entity owning the dialog
   * @return the configured image dialog context
   */
  public static DialogContext imageDialogContext(
    String imagePath,
    TransitionSpeed speed,
    float maxSize,
    ShowImageText textConfig,
    int ownerEntityId) {
    DialogContext.Builder builder =
      DialogContext.builder()
        .type(DialogType.DefaultTypes.IMAGE)
        .put(DialogContextKeys.IMAGE, imagePath)
        .put(DialogContextKeys.IMAGE_TRANSITION_SPEED, speed)
        .put(DialogContextKeys.IMAGE_MAX_SIZE, maxSize)
        .put(DialogContextKeys.OWNER_ENTITY, ownerEntityId);

    putImageText(builder, textConfig);

    return builder.build();
  }

  /**
   * Resolves an inventory-style dialog title from the context or from the entity.
   *
   * @param ctx the dialog context containing an optional title
   * @param titleKey the context key to read the title from
   * @param entity the entity used for the default title
   * @return the configured title, player name, entity name, or {@code Inventory}
   */
  public static String inventoryTitle(DialogContext ctx, String titleKey, Entity entity) {
    return ctx.find(titleKey, String.class).orElse(defaultInventoryTitle(entity));
  }

  /**
   * Resolves the default inventory-style title for an entity.
   *
   * @param entity the entity used for the title
   * @return the player name, entity name, or {@code Inventory}
   */
  public static String defaultInventoryTitle(Entity entity) {
    return entity
      .fetch(PlayerComponent.class)
      .map(PlayerComponent::playerName)
      .filter(name -> !name.isBlank())
      .orElseGet(() -> entity.name().isBlank() ? DEFAULT_INVENTORY_TITLE : entity.name());
  }

  private static void putImageText(DialogContext.Builder builder, ShowImageText textConfig) {
    if (textConfig == null || textConfig.text() == null || textConfig.text().isBlank()) {
      return;
    }

    builder
      .put(DialogContextKeys.IMAGE_TEXT, textConfig.text())
      .put(DialogContextKeys.IMAGE_TEXT_SCALE, textConfig.scale())
      .put(DialogContextKeys.IMAGE_TEXT_COLOR_RGBA8888, textConfig.rgba8888Color());
  }
}
