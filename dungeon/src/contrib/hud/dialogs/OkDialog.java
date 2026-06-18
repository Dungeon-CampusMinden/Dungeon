package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import contrib.hud.UIUtils;
import contrib.hud.elements.RichLabel;
import core.Game;
import core.language.Translation;
import core.utils.BaseContainerUI;

/**
 * Package-private builder for OK dialogs.
 *
 * <p>Creates simple confirmation dialogs with a single OK button. Use {@link
 * DialogFactory#showOkDialog} instead of accessing this class directly.
 */
final class OkDialog {
  static final String T_OK = "ok";
  static final Translation trans = new Translation("dialog.ok_dialog");

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
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("");

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(title, text, trans.text(T_OK));
    }

    return create(UIUtils.defaultSkin(), title, text, ctx.dialogId());
  }

  private static Group create(Skin skin, String title, String text, String dialogId) {
    String ok_text = trans.text(T_OK);
    Dialog dialog =
        new HandledDialog(
            title,
            skin,
            (d, id) -> {
              if (id.equals(ok_text)) {
                DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
                    .accept(null);
              }
              return true;
            });

    DialogDesign.setDialogDefaults(dialog, title);
    Table content = dialog.getContentTable();

    RichLabel label =
        new RichLabel(RichLabel.toRichText(text), DialogDesign.DIALOG_FONT_SPEC_NORMAL);
    label.setWrap(true);
    label.setMaxPrefWidth(675);
    content.add(label).padBottom(10).row();
    dialog.button(ok_text, ok_text, skin.get("green", TextButton.TextButtonStyle.class));

    dialog.pack();

    return new BaseContainerUI(dialog);
  }
}
