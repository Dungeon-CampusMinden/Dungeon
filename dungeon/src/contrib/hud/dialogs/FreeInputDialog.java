package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import core.Entity;
import core.Game;
import java.util.function.Consumer;

final class FreeInputDialog {

  private static final String TITLE_DEFAULT = "Frage";
  private static final String OK_BUTTON = "OK";
  private static final String CANCEL_BUTTON = "Abbrechen";
  private static final String INPUT_PLACEHOLDER_DEFAULT = "Deine Antwort…";

  private FreeInputDialog() {}

  /**
   * Creates and shows a text input dialog with the given title and question. The dialog is added as
   * a HUD-Entity to the game and is displayed on the screen.
   *
   * @param context The dialog context containing the title, question, callback, and other settings.
   * @return The created Dialog instance.
   */
  static Dialog build(DialogContext context) {
    String title = context.find(DialogContextKeys.TITLE, String.class).orElse(TITLE_DEFAULT);
    String question = context.require(DialogContextKeys.QUESTION, String.class);
    @SuppressWarnings("unchecked")
    Consumer<String> callback =
        context.requireCallback(DialogContextKeys.INPUT_CALLBACK, Consumer.class);
    Entity uiEntity = context.requireEntity(DialogContextKeys.ENTITY);
    Skin skin = context.skin();
    Dialog dialog = buildDialog(title, question, callback, uiEntity, skin, context);
    dialog.setSize(700, 350);
    return dialog;
  }

  /**
   * Builds the actual Scene2D Dialog instance with a label, a text field and OK/Cancel buttons.
   *
   * @param title The dialog window title.
   * @param question The text shown as the question/prompt.
   * @param callback A consumer that receives the players trimmed answer; "" if canceled or empty.
   * @param uiEntity The UI-Entity that hosts this dialog; will be removed on close.
   * @param skin The skin to be used for the dialog.
   * @param context The dialog context containing additional settings and preferences.
   * @return The configured, centered Dialog ready to be displayed.
   */
  private static Dialog buildDialog(
      String title,
      String question,
      Consumer<String> callback,
      Entity uiEntity,
      Skin skin,
      DialogContext context) {

    TextField input =
        new TextField(context.find(DialogContextKeys.INPUT_PREFILL, String.class).orElse(""), skin);
    input.setMessageText(
        context
            .find(DialogContextKeys.INPUT_PLACEHOLDER, String.class)
            .orElse(INPUT_PLACEHOLDER_DEFAULT));

    Dialog dialog =
        new Dialog(title, skin) {
          // result() is always called when a button is clicked on
          @Override
          protected void result(Object obj) {
            String value = "";
            if (context
                .find(DialogContextKeys.CONFIRM_LABEL, String.class)
                .orElse(OK_BUTTON)
                .equals(obj)) {
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

    String okLabel = context.find(DialogContextKeys.CONFIRM_LABEL, String.class).orElse(OK_BUTTON);
    String cancelLabel =
        context.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse(CANCEL_BUTTON);
    dialog.button(okLabel, okLabel);
    dialog.button(cancelLabel, cancelLabel);

    return dialog;
  }
}
