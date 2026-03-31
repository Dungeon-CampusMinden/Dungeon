package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;

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

    return GdxDialogBuilderSupport.build(
      GdxDialogBuilderSupport.headless(
        title, question, GdxFreeInputDialog.OK_BUTTON, GdxFreeInputDialog.CANCEL_BUTTON),
      () ->
        GdxFreeInputDialog.create(
          GdxDialogBuilderSupport.defaultSkin(), title, question, ctx));
  }
}
