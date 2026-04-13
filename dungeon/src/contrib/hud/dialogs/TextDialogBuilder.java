package contrib.hud.dialogs;

import core.ui.overlay.OverlayUiNodeHandle;

/** Builds the LITIENGINE-backed text dialog. */
public final class TextDialogBuilder {

  private static final String DEFAULT_CONFIRM_LABEL = "Ok";

  private TextDialogBuilder() {}

  public static core.ui.UiNodeHandle build(DialogContext ctx) {
    String title = ctx.require(DialogContextKeys.TITLE, String.class);
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String confirmLabel =
      ctx.find(DialogContextKeys.CONFIRM_LABEL, String.class).orElse(DEFAULT_CONFIRM_LABEL);
    String cancelLabel =
      ctx.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse(null);
    String[] additionalButtons =
      ctx.find(DialogContextKeys.ADDITIONAL_BUTTONS, String[].class).orElse(new String[] {});

    return new OverlayUiNodeHandle(
      new TextDialogOverlay(
        title, text, confirmLabel, cancelLabel, additionalButtons, ctx.dialogId()));
  }
}
