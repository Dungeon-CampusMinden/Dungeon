package contrib.hud;

import contrib.components.UIComponent;
import contrib.hud.dialogs.*;
import contrib.utils.components.showImage.ShowImageUI;
import contrib.utils.components.showImage.TransitionSpeed;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;

/**
 * The DialogUtils class is responsible for displaying text popups and quizzes to the player.
 *
 * @see DialogFactory
 */
public class DialogUtils {

  /**
   * Displays a text popup.
   *
   * @param text The text of the popup.
   * @param title The title of the popup.
   * @param targetIds The target entity IDs for which the popup is displayed.
   * @return The popup entity.
   * @see DialogFactory#showOkDialog(String, String, IVoidFunction) showOkDialog
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
   * @see DialogFactory#showOkDialog(String, String, IVoidFunction) showOkDialog
   */
  public static Entity showTextPopup(
      String text, String title, IVoidFunction onFinished, int... targetIds) {
    // removes newlines and empty spaces and multiple spaces from the title and text
    title = title.replaceAll("\\s+", " ").trim();
    text = text.replaceAll("\\s+", " ").trim();
    UIComponent ui = DialogFactory.showOkDialog(text, title, onFinished, targetIds);
    return ui.dialogContext().ownerEntity();
  }

  /**
   * Displays an image in a popup with a specified transition speed and an optional close callback.
   *
   * @param imagePath the path to the image to display
   * @param speed the transition speed for showing and hiding the image
   * @param onClose the callback function to execute when the popup is closed
   * @see ShowImageUI
   */
  public static void showImagePopUp(
      String imagePath, TransitionSpeed speed, IVoidFunction onClose) {
    Entity dialogEntity = new Entity();
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.IMAGE)
            .put(DialogContextKeys.IMAGE, imagePath)
            .put(DialogContextKeys.IMAGE_TRANSITION_SPEED, speed)
            .put(DialogContextKeys.OWNER_ENTITY, dialogEntity.id())
            .build();
    UIComponent ui = new UIComponent(context, true, true, new int[] {});
    // Register close callback
    ui.onClose((uic) -> {
      onClose.execute();
    });
    dialogEntity.add(ui);
    Game.add(dialogEntity);
  }

  /**
   * Displays an image in a popup.
   *
   * @param imagePath The path to the image to display. *
   * @param onClose the callback function to execute when the popup is closed
   * @see ShowImageUI
   */
  public static void showImagePopUp(String imagePath, IVoidFunction onClose) {
    showImagePopUp(imagePath, TransitionSpeed.MEDIUM, onClose);
  }

  /**
   * Displays an image in a popup.
   *
   * @param imagePath The path to the image to display.
   * @see ShowImageUI
   */
  public static void showImagePopUp(String imagePath) {
    showImagePopUp(imagePath, TransitionSpeed.MEDIUM, () -> {});
  }
}
