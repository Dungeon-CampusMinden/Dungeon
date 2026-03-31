package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;

/** Builds the libGDX-backed yes/no dialog. */
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

    return GdxDialogBuilderSupport.build(
      GdxDialogBuilderSupport.headless(
        title, text, YesNoDialog.DEFAULT_DIALOG_NO, YesNoDialog.DEFAULT_DIALOG_YES),
      () ->
        YesNoDialog.create(
          GdxDialogBuilderSupport.defaultSkin(), text, title, ctx.dialogId()));
  }
}
