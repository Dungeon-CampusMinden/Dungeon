package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;

/**
 * Package-private Scene2D free input dialog.
 *
 * <p>Creates a dialog with a text field for user input and OK/Cancel buttons.
 */
final class GdxFreeInputDialog {
  static final String TITLE_DEFAULT = "Frage";
  static final String OK_BUTTON = "OK";
  static final String CANCEL_BUTTON = "Abbrechen";
  static final String INPUT_PLACEHOLDER_DEFAULT = "Deine Antwort…";

  private GdxFreeInputDialog() {}

  /**
   * Creates the actual Scene2D dialog instance.
   *
   * @param skin the dialog skin
   * @param title the dialog title
   * @param question the prompt shown above the input field
   * @param context the dialog context
   * @return the configured dialog
   */
  static Dialog create(Skin skin, String title, String question, DialogContext context) {
    TextField input =
      new TextField(
        context.find(DialogContextKeys.INPUT_PREFILL, String.class).orElse(""), skin);
    input.setMessageText(
      context
        .find(DialogContextKeys.INPUT_PLACEHOLDER, String.class)
        .orElse(INPUT_PLACEHOLDER_DEFAULT));

    String okLabel = context.find(DialogContextKeys.CONFIRM_LABEL, String.class).orElse(OK_BUTTON);
    String cancelLabel =
      context.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse(CANCEL_BUTTON);

    Dialog dialog =
      new Dialog(title, skin) {
        @Override
        protected void result(Object obj) {
          if (obj.equals(okLabel)) {
            String userInput = input.getText();
            DialogCallbackResolver.createButtonCallback(
                context.dialogId(), DialogContextKeys.INPUT_CALLBACK)
              .accept(userInput);
          } else {
            DialogCallbackResolver.createButtonCallback(
                context.dialogId(), DialogContextKeys.ON_CANCEL)
              .accept(null);
          }
        }
      };

    dialog.setModal(true);
    dialog.setMovable(false);
    dialog.setResizable(false);
    dialog.setKeepWithinStage(true);

    Table content = dialog.getContentTable();
    content.pad(16);

    Label questionLabel = new Label(question, skin);
    questionLabel.setWrap(true);
    questionLabel.setAlignment(Align.center);

    content.add(questionLabel).width(600).padBottom(16).row();
    content.add(input).width(600).row();

    dialog.getButtonTable().padTop(16);
    dialog.button(cancelLabel, cancelLabel);
    dialog.button(okLabel, okLabel);

    dialog.key(Input.Keys.ENTER, okLabel);
    dialog.key(Input.Keys.ESCAPE, cancelLabel);

    dialog.setSize(700, 350);
    dialog.pack();
    return dialog;
  }
}
