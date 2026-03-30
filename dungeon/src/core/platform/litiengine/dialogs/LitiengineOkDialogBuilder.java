package core.platform.litiengine.dialogs;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.ui.UiNodeHandle;
import core.ui.litiengine.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed OK dialog. */
public final class LitiengineOkDialogBuilder {

  private LitiengineOkDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("OK");

    return new LitiengineUiNodeHandle(
      new LitiengineOkDialogOverlay(title, text, ctx.dialogId()));
  }
}
