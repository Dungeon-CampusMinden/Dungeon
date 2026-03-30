package core.platform.litiengine.dialogs;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.ui.UiNodeHandle;
import core.ui.litiengine.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed text dialog. */
public final class LitiengineTextDialogBuilder {

  private static final String DEFAULT_CONFIRM_LABEL = "Ok";

  private LitiengineTextDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    String title = ctx.require(DialogContextKeys.TITLE, String.class);
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String confirmLabel =
      ctx.find(DialogContextKeys.CONFIRM_LABEL, String.class).orElse(DEFAULT_CONFIRM_LABEL);
    String cancelLabel =
      ctx.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse(null);
    String[] additionalButtons =
      ctx.find(DialogContextKeys.ADDITIONAL_BUTTONS, String[].class).orElse(new String[] {});

    return new LitiengineUiNodeHandle(
      new LitiengineTextDialogOverlay(
        title, text, confirmLabel, cancelLabel, additionalButtons, ctx.dialogId()));
  }
}
