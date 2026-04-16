package contrib.hud.showimage;

import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogType;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;

/**
 * A utility class for displaying images in popup dialogs.
 *
 * <p>This class provides convenient static methods to show images in dialogs with various
 * configuration options.
 *
 * <p>Multiple overloads allow for simple usage with default parameters
 * or full control over transition speed, size constraints, and text overlays.
 */
public final class ShowImageDialogLauncher {

  private ShowImageDialogLauncher() {}

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
    UIComponent ui = new UIComponent(context, true, true);

    ui.onClose((_) -> onClose.execute());

    dialogEntity.add(ui);
    Game.add(dialogEntity);
  }

  /**
   * Displays an image in a popup with a specified transition speed.
   *
   * <p>Uses a default maximum size of 0.85 (85% of screen) and no text overlay.
   *
   * @param imagePath the path to the image to display
   * @param speed the transition speed for showing the image
   * @param onClose callback executed when the popup is closed
   */
  public static void showImagePopUp(
    String imagePath, TransitionSpeed speed, IVoidFunction onClose) {
    showImagePopUp(imagePath, speed, 0.85f, null, onClose);
  }

  /**
   * Displays an image in a popup with the default transition speed.
   *
   * <p>Uses a default maximum size of 0.85 (85% of screen) and medium transition speed.
   *
   * @param imagePath the path to the image to display
   * @param onClose callback executed when the popup is closed
   */
  public static void showImagePopUp(String imagePath, IVoidFunction onClose) {
    showImagePopUp(imagePath, TransitionSpeed.MEDIUM, 0.85f, null, onClose);
  }

  /**
   * Displays an image in a popup with all default settings.
   *
   * <p>Uses default maximum size of 0.85 (85% of screen) and medium transition speed.
   * The popup closes silently without executing any callback.
   *
   * @param imagePath the path to the image to display
   */
  public static void showImagePopUp(String imagePath) {
    showImagePopUp(imagePath, TransitionSpeed.MEDIUM, 0.85f, null, () -> {});
  }
}
