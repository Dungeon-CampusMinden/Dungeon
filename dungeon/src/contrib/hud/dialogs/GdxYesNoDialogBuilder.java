package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.UIUtils;
import core.Game;

/**
 * Builds the libGDX-backed yes/no dialog.
 */
public final class GdxYesNoDialogBuilder {

  private GdxYesNoDialogBuilder() {}

  /**
   * Builds a Scene2D yes/no dialog from the given context.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the yes/no dialog
   */
  public static Group build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("Dialog");

    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(
        title, text, YesNoDialog.DEFAULT_DIALOG_NO, YesNoDialog.DEFAULT_DIALOG_YES);
    }

    return YesNoDialog.create(UIUtils.defaultSkin(), text, title, ctx.dialogId());
  }
}
