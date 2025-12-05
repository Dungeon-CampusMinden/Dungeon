package contrib.hud;

import contrib.components.UIComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
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
   * @return The popup entity.
   * @see DialogFactory#showOkDialog(String, String, IVoidFunction) showOkDialog
   */
  public static Entity showTextPopup(String text, String title) {
    return showTextPopup(text, title, () -> {});
  }

  /**
   * Displays a text popup. Upon closing the popup, the onFinished function is executed.
   *
   * @param text The text of the popup.
   * @param title The title of the popup.
   * @param onFinished The function to execute when the popup is closed.
   * @return The popup entity.
   * @see DialogFactory#showOkDialog(String, String, IVoidFunction) showOkDialog
   */
  public static Entity showTextPopup(String text, String title, IVoidFunction onFinished) {
    // removes newlines and empty spaces and multiple spaces from the title and text
    title = title.replaceAll("\\s+", " ").trim();
    text = text.replaceAll("\\s+", " ").trim();
    return DialogFactory.show(
        DialogContext.builder()
            .type(DialogType.DefaultTypes.OK)
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.MESSAGE, text)
            .putCallback(DialogContextKeys.ON_CONFIRM, onFinished)
            .build());
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
    Entity e = new Entity();
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.IMAGE)
            .put(DialogContextKeys.IMAGE, imagePath)
            .put(DialogContextKeys.IMAGE_TRANSITION_SPEED, speed)
            .build();
    UIComponent ui = new UIComponent(context, true, true, new int[] {});
    ui.onClose(onClose);
    e.add(ui);
    Game.add(e);
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
