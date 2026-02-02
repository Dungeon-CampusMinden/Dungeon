package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import contrib.hud.UIUtils;
import core.Game;
import core.network.messages.c2s.DialogResponseMessage;
import core.utils.BaseContainerUI;
import core.utils.Scene2dElementFactory;

/**
 * Package-private builder for free text input dialogs.
 *
 * <p>Creates a dialog with a text field for user input and OK/Cancel buttons.
 */
final class FreeInputDialog {

  /** Callback key for the input submission callback. */
  public static final String CALLBACK_INPUT = "input";

  private static final String TITLE_DEFAULT = "Frage";
  private static final String OK_BUTTON = "OK";
  private static final String CANCEL_BUTTON = "Abbrechen";
  private static final String INPUT_PLACEHOLDER_DEFAULT = "Deine Antwort…";

  private FreeInputDialog() {}

  /**
   * Creates and shows a text input dialog with the given title and question.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * <p>Callbacks should be registered on UIComponent:
   *
   * <ul>
   *   <li>{@code CALLBACK_INPUT} - receives the user's input text (String)
   * </ul>
   *
   * @param ctx The dialog context containing the title, question, and other settings.
   * @return The created Dialog instance or HeadlessDialogGroup.
   */
  static Group build(DialogContext ctx) {
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse(TITLE_DEFAULT);
    String question = ctx.find(DialogContextKeys.QUESTION, String.class).orElse("");

    // On headless server, return placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(title, question, OK_BUTTON, CANCEL_BUTTON);
    }

    Skin skin = UIUtils.defaultSkin();
    return buildDialog(title, question, skin, ctx);
  }

  /**
   * Builds the actual Scene2D Dialog instance with a label, a text field and OK/Cancel buttons.
   *
   * @param title The dialog window title.
   * @param question The text shown as the question/prompt.
   * @param skin The skin to be used for the dialog.
   * @param context The dialog context containing additional settings and preferences.
   * @return The configured, centered Dialog ready to be displayed.
   */
  private static Group buildDialog(
      String title, String question, Skin skin, DialogContext context) {

    TextField input =
        new TextField(context.find(DialogContextKeys.INPUT_PREFILL, String.class).orElse(""), skin);
    input.setMessageText(
        context
            .find(DialogContextKeys.INPUT_PLACEHOLDER, String.class)
            .orElse(INPUT_PLACEHOLDER_DEFAULT));

    String okLabel = context.find(DialogContextKeys.CONFIRM_LABEL, String.class).orElse(OK_BUTTON);
    String cancelLabel =
        context.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse(CANCEL_BUTTON);

    boolean hasTitle = title != null && !title.isBlank();
    Dialog dialog =
        new Dialog(title, skin, hasTitle ? "default" : "no-title") {
          @Override
          protected void result(Object obj) {
            if (obj.equals(okLabel)) {
              String userInput = input.getText();
              DialogCallbackResolver.createButtonCallback(
                      context.dialogId(), DialogContextKeys.INPUT_CALLBACK)
                  .accept(new DialogResponseMessage.StringValue(userInput));
            } else {
              DialogCallbackResolver.createButtonCallback(
                      context.dialogId(), DialogContextKeys.ON_CANCEL)
                  .accept(null);
            }
          }
        };

    DialogDesign.setDialogDefaults(dialog, title);

    Table content = dialog.getContentTable();

    if (!question.isBlank()) {
      content
          .add(Scene2dElementFactory.createLabel(question, DialogDesign.DIALOG_FONT_SPEC_NORMAL))
          .padBottom(10)
          .row();
    }

    content.add(input).width(400).padBottom(10).row();

    dialog.button(okLabel, okLabel, skin.get("clean-green", TextButton.TextButtonStyle.class));
    dialog.button(
        cancelLabel, cancelLabel, skin.get("clean-red-outline", TextButton.TextButtonStyle.class));

    dialog.pack();

    return new BaseContainerUI(dialog);
  }
}
