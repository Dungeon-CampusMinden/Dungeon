package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import core.Game;

/**
 * Package-private builder for OK dialogs.
 *
 * <p>Creates simple confirmation dialogs with a single OK button. Use {@link
 * DialogFactory#showOkDialog} instead of accessing this class directly.
 */
final class OkDialog {
  static final String DEFAULT_OK_BUTTON = "Ok";

  private OkDialog() {}

  /**
   * Builds an OK dialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx The dialog context containing the message, title, and confirmation callback
   * @return A fully configured OK dialog or HeadlessDialogGroup
   */
  static Group build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("OK");

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(title, text, DEFAULT_OK_BUTTON);
    }

    return create(UIUtils.defaultSkin(), title, text, ctx.dialogId());
  }

  private static Dialog create(Skin skin, String title, String text, String dialogId) {
    Dialog textDialog =
        new TextDialog(
            title,
            skin,
            "Letter",
            (d, id) -> {
              if (id.equals(DEFAULT_OK_BUTTON)) {
                DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
                    .accept(null);
              }
              return true;
            });
    textDialog.getContentTable().add(DialogDesign.createTextDialog(skin, text)).center().grow();
    textDialog.button(DEFAULT_OK_BUTTON, DEFAULT_OK_BUTTON);
    textDialog.pack();
    return textDialog;
  }
}
