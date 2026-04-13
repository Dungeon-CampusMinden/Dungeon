package contrib.hud.dialogs;

import core.ui.overlay.OverlayUiNodeHandle;

/** Builds the LITIENGINE-backed yes/no dialog. */
public final class YesNoDialogBuilder {

  private YesNoDialogBuilder() {}

  public static core.ui.UiNodeHandle build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("Dialog");

    return new OverlayUiNodeHandle(
      new YesNoDialogOverlay(title, text, ctx.dialogId()));
  }
}
