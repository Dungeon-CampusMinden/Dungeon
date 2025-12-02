package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Package-private builder for Yes/No dialogs.
 *
 * <p>Creates confirmation dialogs with Yes and No buttons. Use {@link
 * DialogFactory#showYesNoDialog} instead of accessing this class directly.
 */
final class YesNoDialog {
  private static final String DEFAULT_DIALOG_YES = "Ja";
  private static final String DEFAULT_DIALOG_NO = "Nein";

  private YesNoDialog() {}

  /**
   * Builds a Yes/No dialog from the given context.
   *
   * @param context The dialog context containing the message, title, and yes/no callbacks
   * @return A fully configured Yes/No dialog
   */
  static Dialog build(DialogContext context) {
    String text = context.require(DialogContextKeys.MESSAGE, String.class);
    String title = context.find(DialogContextKeys.TITLE, String.class).orElse("Dialog");
    IVoidFunction onYes = context.require(DialogContextKeys.ON_YES, IVoidFunction.class);
    IVoidFunction onNo = context.require(DialogContextKeys.ON_NO, IVoidFunction.class);
    Entity entity = context.require(DialogContextKeys.ENTITY, Entity.class);
    Dialog dialog =
        createYesNoDialog(
            context.skin(), text, title, createResultHandlerYesNo(entity, onYes, onNo));
    dialog.pack();
    return dialog;
  }

  private static Dialog createYesNoDialog(
      final Skin skin,
      final String text,
      final String title,
      final BiFunction<Dialog, String, Boolean> resultHandler) {
    Dialog textDialog = new TextDialog(title, skin, "Letter", resultHandler);
    textDialog
        .getContentTable()
        .add(DialogDesign.createTextDialog(skin, UIUtils.formatString(text)))
        .center()
        .grow();
    textDialog.button(DEFAULT_DIALOG_NO, DEFAULT_DIALOG_NO);
    textDialog.button(DEFAULT_DIALOG_YES, DEFAULT_DIALOG_YES);
    return textDialog;
  }

  private static BiFunction<Dialog, String, Boolean> createResultHandlerYesNo(
      final Entity entity, final IVoidFunction onYes, final IVoidFunction onNo) {
    return (d, id) -> {
      if (Objects.equals(id, DEFAULT_DIALOG_YES)) {
        onYes.execute();
        Game.remove(entity);
        return true;
      }
      if (Objects.equals(id, DEFAULT_DIALOG_NO)) {
        onNo.execute();
        Game.remove(entity);
        return true;
      }
      return false;
    };
  }
}
