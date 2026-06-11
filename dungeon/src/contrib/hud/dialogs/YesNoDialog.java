package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import contrib.hud.UIUtils;
import core.Game;
import core.utils.BaseContainerUI;
import core.utils.Scene2dElementFactory;

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
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("");

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(title, text, DEFAULT_DIALOG_NO, DEFAULT_DIALOG_YES);
    }

    return create(UIUtils.defaultSkin(), title, text, ctx.dialogId());
  }

  private static Group create(Skin skin, String title, String text, String dialogId) {
    Dialog dialog =
        new HandledDialog(
            title,
            skin,
            (d, id) -> {
              if (id.equals(DEFAULT_DIALOG_YES)) {
                DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_YES)
                    .accept(null);
              } else if (id.equals(DEFAULT_DIALOG_NO)) {
                DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_NO)
                    .accept(null);
              }
              return true;
            });

    DialogDesign.setDialogDefaults(dialog, title);
    Table content = dialog.getContentTable();

    content
        .add(Scene2dElementFactory.createLabel(text, DialogDesign.DIALOG_FONT_SPEC_NORMAL))
        .padBottom(10)
        .row();
    dialog.button(
        DEFAULT_DIALOG_YES,
        DEFAULT_DIALOG_YES,
        skin.get("clean-green", TextButton.TextButtonStyle.class));
    dialog.button(
        DEFAULT_DIALOG_NO,
        DEFAULT_DIALOG_NO,
        skin.get("clean-red-outline", TextButton.TextButtonStyle.class));

    dialog.pack();

    return new BaseContainerUI(dialog);
  }
}
