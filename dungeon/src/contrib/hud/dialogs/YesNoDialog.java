package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import contrib.hud.UIUtils;
import core.Game;
import core.language.Translation;
import core.utils.BaseContainerUI;
import core.utils.Scene2dElementFactory;

/**
 * Package-private builder for Yes/No dialogs.
 *
 * <p>Creates confirmation dialogs with Yes and No buttons. Use {@link
 * DialogFactory#showYesNoDialog} instead of accessing this class directly.
 */
final class YesNoDialog {
  private static final String T_YES = "yes";
  private static final String T_NO = "no";
  private static final Translation trans = new Translation("dialog.yes_no_dialog");

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
    String yesLabel = trans.text(T_YES);
    String noLabel = trans.text(T_NO);

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(title, text, noLabel, yesLabel);
    }

    return create(UIUtils.defaultSkin(), title, text, ctx.dialogId(), yesLabel, noLabel);
  }

  private static Group create(
      Skin skin, String title, String text, String dialogId, String yesLabel, String noLabel) {
    Dialog dialog =
        new HandledDialog(
            title,
            skin,
            (d, id) -> {
              if (id.equals(yesLabel)) {
                DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_YES)
                    .accept(null);
              } else if (id.equals(noLabel)) {
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
    dialog.button(yesLabel, yesLabel, skin.get("green", TextButton.TextButtonStyle.class));
    dialog.button(noLabel, noLabel, skin.get("red-outline", TextButton.TextButtonStyle.class));

    dialog.pack();

    return new BaseContainerUI(dialog);
  }
}
