package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.Entity;
import core.Game;
import java.util.function.Consumer;

/**
 * A dialog helper for collecting free-form text input from the player.
 *
 * <p>Use {@link #showTextInputDialog(String, String, Consumer)} to create and add a HUD-Entity to
 * the game that shows a modal dialog with a question, a text field, and OK/Cancel buttons.
 */
public final class FreeInputDialog {

  private static final String OK_BUTTON = "OK";
  private static final String CANCEL_BUTTON = "Abbrechen";

  /**
   * Creates and shows a text input dialog with the given title and question. The dialog is added as
   * a HUD-Entity to the game and is displayed on the screen.
   *
   * @param title The window title shown at the top of the dialog.
   * @param question The prompt text displayed above the input field.
   * @param callback A consumer that receives the player's answer (trimmed) or null.
   */
  public static void showTextInputDialog(String title, String question, Consumer<String> callback) {
    final Entity uiEntity = new Entity();

    UIUtils.show(() -> buildDialog(title, question, callback, uiEntity), uiEntity);
    Game.add(uiEntity);
  }

  /**
   * Builds the actual Scene2D Dialog instance with a label, a text field and OK/Cancel buttons.
   *
   * @param title The dialog window title.
   * @param question The text shown as the question/prompt.
   * @param callback A consumer that receives the players trimmed answer; "" if canceled or empty.
   * @param uiEntity The UI-Entity that hosts this dialog; will be removed on close.
   * @return The configured, centered Dialog ready to be displayed.
   */
  private static Dialog buildDialog(
      String title, String question, Consumer<String> callback, Entity uiEntity) {

    Skin skin = UIUtils.defaultSkin();
    TextField input = new TextField("", skin);
    input.setMessageText("Deine Antwort…");

    Dialog dialog =
        new Dialog(title, skin) {
          // result() is always called when a button is clicked on
          @Override
          protected void result(Object obj) {
            String value = "";
            if (OK_BUTTON.equals(obj)) {
              value = input.getText().trim();
            }

            callback.accept(value);
            Game.remove(uiEntity);
          }
        };

    dialog.setModal(true);
    dialog.setMovable(false);
    dialog.setResizable(false);
    dialog.setKeepWithinStage(true);

    Table content = dialog.getContentTable();
    content.pad(16);
    content.add(new Label(question, skin)).align(Align.center).padBottom(10).row();
    content.add(input).width(200).padBottom(10).row();

    dialog.button(OK_BUTTON, OK_BUTTON);
    dialog.button(CANCEL_BUTTON, CANCEL_BUTTON);
    dialog.setSize(700, 350);
    UIUtils.center(dialog);

    return dialog;
  }
}
