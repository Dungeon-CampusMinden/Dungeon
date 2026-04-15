package contrib.hud.dialogs;

import core.ui.UiNodeHandle;
import core.ui.overlay.OverlayUiNodeHandle;

/**
 * A builder for creating free text input dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display a text input dialog overlay.
 *
 * <p>It retrieves the question text and optional configuration from the dialog context, including
 * title, placeholder text, and button labels. Default values are used for any unspecified parameters.
 */
public final class FreeInputDialogBuilder {

  private FreeInputDialogBuilder() {}

  /**
   * Builds a UI node handle for a free text input dialog overlay.
   *
   * <p>This method requires the dialog context to contain a question text.
   *
   * <p>It retrieves optional configuration parameters from the context or uses sensible defaults:
   * <ul>
   *   <li>Title defaults to the overlay's default title</li>
   *   <li>Prefill defaults to an empty string</li>
   *   <li>Placeholder defaults to the overlay's default placeholder text</li>
   *   <li>Confirm label defaults to "OK"</li>
   *   <li>Cancel label defaults to "CANCEL"</li>
   * </ul>
   *
   * @param ctx the dialog context containing the question and optional configuration
   * @return a UI node handle wrapping the created free input dialog overlay
   * @throws IllegalArgumentException if the required question is not present in the context
   */
  public static UiNodeHandle build(DialogContext ctx) {
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
