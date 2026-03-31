package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.HeadlessDialogGroup;
import core.Game;

/** Builds the libGDX-backed free input dialog. */
public final class GdxFreeInputDialogBuilder {

  private GdxFreeInputDialogBuilder() {}

  /**
   * Builds a Scene2D free-input dialog from the given context.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the free-input dialog
   */
  public static Group build(DialogContext ctx) {
    String title =
      ctx.find(DialogContextKeys.TITLE, String.class).orElse(GdxFreeInputDialog.TITLE_DEFAULT);
    String question = ctx.require(DialogContextKeys.QUESTION, String.class);

    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(
        title,
        question,
        GdxFreeInputDialog.OK_BUTTON,
        GdxFreeInputDialog.CANCEL_BUTTON);
    }

    return GdxFreeInputDialog.create(UIUtils.defaultSkin(), title, question, ctx);
  }
}
