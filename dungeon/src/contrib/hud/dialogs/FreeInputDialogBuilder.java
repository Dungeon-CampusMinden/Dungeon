package contrib.hud.dialogs;

import core.ui.overlay.OverlayUiNodeHandle;

/** Builds the LITIENGINE-backed free-input dialog. */
public final class FreeInputDialogBuilder {

  private FreeInputDialogBuilder() {}

  public static core.ui.UiNodeHandle build(DialogContext ctx) {
    String title =
      ctx.find(DialogContextKeys.TITLE, String.class)
        .orElse(FreeInputDialogOverlay.TITLE_DEFAULT);
    String question = ctx.require(DialogContextKeys.QUESTION, String.class);
    String prefill = ctx.find(DialogContextKeys.INPUT_PREFILL, String.class).orElse("");
    String placeholder =
      ctx.find(DialogContextKeys.INPUT_PLACEHOLDER, String.class)
        .orElse(FreeInputDialogOverlay.INPUT_PLACEHOLDER_DEFAULT);
    String confirmLabel =
      ctx.find(DialogContextKeys.CONFIRM_LABEL, String.class)
        .orElse(FreeInputDialogOverlay.OK_BUTTON);
    String cancelLabel =
      ctx.find(DialogContextKeys.CANCEL_LABEL, String.class)
        .orElse(FreeInputDialogOverlay.CANCEL_BUTTON);

    return new OverlayUiNodeHandle(
      new FreeInputDialogOverlay(
        title, question, prefill, placeholder, confirmLabel, cancelLabel, ctx.dialogId()));
  }
}
