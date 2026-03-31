package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;

/** Builds the libGDX-backed OK dialog. */
public final class GdxOkDialogBuilder {

  private GdxOkDialogBuilder() {}

  /**
   * Builds a Scene2D OK dialog from the given context.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the OK dialog
   */
  public static Group build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("OK");

    return GdxDialogBuilderSupport.build(
      GdxDialogBuilderSupport.headless(title, text, OkDialog.DEFAULT_OK_BUTTON),
      () -> OkDialog.create(GdxDialogBuilderSupport.defaultSkin(), title, text, ctx.dialogId()));
  }
}
