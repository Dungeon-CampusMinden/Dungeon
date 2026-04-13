package contrib.hud.image;

import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;

/**
 * Entry points for show-image dialogs.
 *
 * <p>This class centralizes the creation of image popups so that the public API for show-image dialogs
 * lives next to the corresponding dialog payload and backend-specific implementations.
 */
public final class ShowImageDialogs {

  private ShowImageDialogs() {}

  /**
   * Displays an image in a popup with the full image-dialog payload.
   *
   * @param imagePath the path to the image to display
   * @param speed the transition speed for showing the image
   * @param maxSize the maximum size factor of the image relative to the screen
   * @param textConfig optional text configuration rendered on top of the image
   * @param onClose callback executed when the popup is closed
   */
  public static void showImagePopUp(
    String imagePath,
    TransitionSpeed speed,
    float maxSize,
    ShowImageText textConfig,
    IVoidFunction onClose) {

    Entity dialogEntity = new Entity();

    var builder =
      DialogContext.builder()
        .type(DialogType.DefaultTypes.IMAGE)
        .put(DialogContextKeys.IMAGE, imagePath)
        .put(DialogContextKeys.IMAGE_TRANSITION_SPEED, speed)
        .put(DialogContextKeys.IMAGE_MAX_SIZE, maxSize)
        .put(DialogContextKeys.OWNER_ENTITY, dialogEntity.id());

    if (textConfig != null && textConfig.text() != null && !textConfig.text().isBlank()) {
      builder
        .put(DialogContextKeys.IMAGE_TEXT, textConfig.text())
        .put(DialogContextKeys.IMAGE_TEXT_SCALE, textConfig.scale())
        .put(DialogContextKeys.IMAGE_TEXT_COLOR_RGBA8888, textConfig.rgba8888Color());
    }

    DialogContext context = builder.build();
    UIComponent ui = new UIComponent(context, true, true, new int[] {});

    ui.onClose((uic) -> onClose.execute());

    dialogEntity.add(ui);
    Game.add(dialogEntity);
  }

  public static void showImagePopUp(
    String imagePath, TransitionSpeed speed, IVoidFunction onClose) {
    showImagePopUp(imagePath, speed, 0.85f, null, onClose);
  }

  public static void showImagePopUp(String imagePath, IVoidFunction onClose) {
    showImagePopUp(imagePath, TransitionSpeed.MEDIUM, 0.85f, null, onClose);
  }

  public static void showImagePopUp(String imagePath) {
    showImagePopUp(imagePath, TransitionSpeed.MEDIUM, 0.85f, null, () -> {});
  }
}
