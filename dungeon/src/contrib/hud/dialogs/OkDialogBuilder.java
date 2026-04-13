package contrib.hud.dialogs;

import core.ui.UiNodeHandle;
import core.ui.overlay.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed OK dialog. */
public final class OkDialogBuilder {

  private OkDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("OK");

    return new LitiengineUiNodeHandle(
      new LitiengineOkDialogOverlay(title, text, ctx.dialogId()));
  }
}
