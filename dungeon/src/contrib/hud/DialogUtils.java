package contrib.hud;

import contrib.components.UIComponent;
import contrib.hud.dialogs.*;
import contrib.hud.dialogs.showimage.ShowImageText;
import contrib.hud.dialogs.showimage.TransitionSpeed;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;

/**
 * The DialogUtils class is responsible for displaying text popups and quizzes to the player.
 *
 * @see DialogService
 */
public class DialogUtils {

  /**
   * Displays a text popup.
   *
   * @param text The text of the popup.
   * @param title The title of the popup.
   * @param targetIds The target entity IDs for which the popup is displayed.
   * @return The popup entity.
   */
  public static Entity showTextPopup(String text, String title, int... targetIds) {
    return showTextPopup(text, title, () -> {}, targetIds);
  }

  /**
   * Displays a text popup. Upon closing the popup, the onFinished function is executed.
   *
   * @param text The text of the popup.
   * @param title The title of the popup.
   * @param onFinished The function to execute when the popup is closed.
   * @param targetIds The target entity IDs for which the popup is displayed.
   * @return The popup entity.
   */
  public static Entity showTextPopup(
      String text, String title, IVoidFunction onFinished, int... targetIds) {
    // removes newlines and empty spaces and multiple spaces from the title and text
    title = title.replaceAll("\\s+", " ").trim();
    text = text.replaceAll("\\s+", " ").trim();
    UIComponent ui = DialogService.showOkDialog(text, title, onFinished, targetIds);
    return ui.dialogContext().ownerEntity();
  }

  /**
   * Displays an image popup with the default transition speed and size.
   *
   * @param imagePath  the path to the image to display
   * @param onFinished the function to execute when the popup is closed
   */
  public static void showImagePopup(String imagePath, IVoidFunction onFinished) {
    showImagePopup(imagePath, TransitionSpeed.MEDIUM, 0.85f, null, onFinished);
  }

  /**
   * Displays an image popup with the given configuration.
   *
   * @param imagePath  the path to the image to display
   * @param speed      the transition speed for showing the image
   * @param maxSize    the maximum size factor of the image relative to the screen
   * @param textConfig optional text configuration rendered on top of the image
   * @param onFinished the function to execute when the popup is closed
   */
  public static void showImagePopup(
      String imagePath,
      TransitionSpeed speed,
      float maxSize,
      ShowImageText textConfig,
      IVoidFunction onFinished) {
    Entity dialogEntity = new Entity();
    DialogContext context =
        DialogContextHelper.imageDialogContext(
            imagePath, speed, maxSize, textConfig, dialogEntity.id());

    Game.add(dialogEntity);
    UIComponent ui = DialogService.show(context);
    ui.onClose((_) -> onFinished.execute());
  }
}
