package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import core.Game;
import java.util.function.BiFunction;

/**
 * Package-private builder for Yes/No dialogs.
 *
 * <p>Creates confirmation dialogs with Yes and No buttons. Use {@link
 * DialogFactory#showYesNoDialog} instead of accessing this class directly.
 */
final class YesNoDialog {
  static final String DEFAULT_DIALOG_YES = "Ja";
  static final String DEFAULT_DIALOG_NO = "Nein";

  private YesNoDialog() {}

  /**
   * Builds a Yes/No dialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx The dialog context containing the message, title, and yes/no callbacks
   * @return A fully configured Yes/No dialog or HeadlessDialogGroup
   */
  static Group build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("Dialog");

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(title, text, DEFAULT_DIALOG_NO, DEFAULT_DIALOG_YES);
    }

    return createYesNoDialog(UIUtils.defaultSkin(), text, title, ctx.dialogId());
  }

  private static Dialog createYesNoDialog(
      final Skin skin, final String text, final String title, String dialogId) {
    BiFunction<Dialog, String, Boolean> resultHandler =
        (d, id) -> {
          if (id.equals(DEFAULT_DIALOG_YES)) {
            DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_YES)
                .accept(null);
          } else if (id.equals(DEFAULT_DIALOG_NO)) {
            DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_NO)
                .accept(null);
          }
          return true;
        };
    Dialog textDialog = new TextDialog(title, skin, "Letter", resultHandler);
    textDialog
        .getContentTable()
        .add(DialogDesign.createTextDialog(skin, UIUtils.formatString(text)))
        .center()
        .grow();
    textDialog.button(DEFAULT_DIALOG_NO, DEFAULT_DIALOG_NO);
    textDialog.button(DEFAULT_DIALOG_YES, DEFAULT_DIALOG_YES);
    textDialog.pack();
    return textDialog;
  }
}
