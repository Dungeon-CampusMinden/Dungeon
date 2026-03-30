package core.platform.litiengine.dialogs;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.ui.UiNodeHandle;
import core.ui.litiengine.LitiengineUiNodeHandle;

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
