package contrib.hud.dialogs;

import core.ui.UiNodeHandle;
import core.ui.overlay.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed yes/no dialog. */
public final class LitiengineYesNoDialogBuilder {

  private LitiengineYesNoDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("Dialog");

    return new LitiengineUiNodeHandle(
      new LitiengineYesNoDialogOverlay(title, text, ctx.dialogId()));
  }
}
