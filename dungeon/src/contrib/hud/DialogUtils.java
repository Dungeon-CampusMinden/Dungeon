package contrib.hud;

import contrib.components.UIComponent;
import contrib.hud.dialogs.*;
import core.Entity;
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
    UIComponent ui = DialogFactory.showOkDialog(text, title, onFinished, targetIds);
    return ui.dialogContext().ownerEntity();
  }
}
