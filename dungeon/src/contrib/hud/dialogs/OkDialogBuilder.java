package contrib.hud.dialogs;

import core.ui.overlay.OverlayUiNodeHandle;

/** Builds the LITIENGINE-backed OK dialog. */
public final class OkDialogBuilder {

  private OkDialogBuilder() {}

  public static core.ui.UiNodeHandle build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("OK");

    return new OverlayUiNodeHandle(
      new OkDialogOverlay(title, text, ctx.dialogId()));
  }
}
