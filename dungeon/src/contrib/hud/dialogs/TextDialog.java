package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import core.Game;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A TextDialog that allows the result handler to be defined per functional interface.
 *
 * <p>Use {@link DialogFactory#showTextDialog} to create and show a TextDialog.
 */
public final class TextDialog extends Dialog {

  /** Handler for Button presses. */
  private final BiFunction<Dialog, String, Boolean> resultHandler;

  /**
   * Creates a TextDialog with the given title and skin and stores the functional interface for
   * Button events.
   *
   * @param skin Skin for the dialog (resources that can be used by UI widgets)
   * @param title Title of the dialog
   * @param resultHandler controls the button presses
   */
  public TextDialog(
      final String title,
      final Skin skin,
      final BiFunction<Dialog, String, Boolean> resultHandler) {
    super(title, skin);
    this.resultHandler = resultHandler;
  }

  /**
   * Creates a TextDialog with the given title and skin and stores the functional interface for
   * Button events.
   *
   * @param title Title of the dialog
   * @param skin Skin for the dialog (resources that can be used by UI widgets)
   * @param windowStyleName the name of the style which should be used
   * @param resultHandler controls the button presses
   */
  public TextDialog(
      final String title,
      final Skin skin,
      final String windowStyleName,
      final BiFunction<Dialog, String, Boolean> resultHandler) {
    super(title, skin, windowStyleName);
    this.resultHandler = resultHandler;
  }

  /**
   * A simple Text Dialog that shows only the provided string.
   *
   * @param skin The style in which the whole dialog should be shown.
   * @param outputMsg The text which should be shown in the middle of the dialog.
   * @param confirmButton Text that the button should have; also the ID for the result handler.
   * @param title Title for the dialog.
   * @param resultHandler A callback method that is called when the confirm button is pressed.
   * @return The fully configured Dialog, which can then be added where it is needed.
   */
  private static Dialog createTextDialog(
      final Skin skin,
      final String outputMsg,
      final String confirmButton,
      final String title,
      final BiFunction<Dialog, String, Boolean> resultHandler) {
    Dialog textDialog = new TextDialog(title, skin, resultHandler);
    textDialog
        .getContentTable()
        .add(DialogDesign.createTextDialog(skin, outputMsg))
        .center()
        .grow();
    textDialog.button(confirmButton, confirmButton);
    return textDialog;
  }

  /**
   * Builds a text dialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx The dialog context containing message, buttons, and handlers
   * @return A fully configured text dialog or HeadlessDialogGroup
   */
  static Group build(DialogContext ctx) {
    String title = ctx.require(DialogContextKeys.TITLE, String.class);
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String button =
        ctx.find(DialogContextKeys.CONFIRM_LABEL, String.class).orElse(OkDialog.DEFAULT_OK_BUTTON);
    String cancelButton = ctx.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse(null);
    String[] extraButtons =
        ctx.find(DialogContextKeys.ADDITIONAL_BUTTONS, String[].class).orElse(new String[] {});

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      List<String> allButtons = new ArrayList<>();
      allButtons.add(button);
      if (cancelButton != null) {
        allButtons.add(cancelButton);
      }
      allButtons.addAll(Arrays.asList(extraButtons));
      return new HeadlessDialogGroup(title, text, allButtons.toArray(new String[0]));
    }

    Skin skin = UIUtils.defaultSkin();
    Dialog dialog =
        TextDialog.createTextDialog(
            skin,
            text,
            button,
            title,
            (d, id) -> {
              if (id.equals(button)) {
                DialogCallbackResolver.createButtonCallback(
                        ctx.dialogId(), DialogContextKeys.ON_CONFIRM)
                    .accept(null);
              } else if (id.equals(cancelButton)) {
                DialogCallbackResolver.createButtonCallback(
                        ctx.dialogId(), DialogContextKeys.ON_CANCEL)
                    .accept(null);
              } else {
                for (String extraButton : extraButtons) {
                  if (id.equals(extraButton)) {
                    DialogCallbackResolver.createButtonCallback(ctx.dialogId(), "on" + extraButton)
                        .accept(null);
                  }
                }
              }
              return true;
            });
    dialog.button(button, button);
    if (cancelButton != null) {
      dialog.button(cancelButton, cancelButton);
    }
    for (String extraButton : extraButtons) {
      dialog.button(extraButton, extraButton);
    }
    dialog.pack();
    return dialog;
  }

  /**
   * When a Button event happens, calls the stored resultHandler. When the resultHandler returns
   * false, stops the default hide on button press.
   *
   * @param object Object associated with the button
   */
  @Override
  protected void result(final Object object) {
    if (!resultHandler.apply(this, object.toString())) cancel();
  }
}
