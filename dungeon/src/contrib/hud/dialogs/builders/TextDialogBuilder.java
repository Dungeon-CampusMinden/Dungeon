package contrib.hud.dialogs.builders;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.overlays.TextDialogOverlay;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;

/**
 * A builder for creating text dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display a text dialog overlay with
 * configurable title, message, and button labels.
 *
 * <p>It supports optional cancel buttons and additional custom buttons for extended functionality.
 */
public final class TextDialogBuilder {

  private static final String DEFAULT_CONFIRM_LABEL = "Ok";
  private static final String DEFAULT_OK_TITLE = "OK";
  private static final String DEFAULT_OK_CONFIRM_LABEL = "OK";

  private TextDialogBuilder() {}

  /**
   * Builds a UI node handle for a text dialog overlay.
   *
   * <p>This method requires the dialog context to contain a title and message text.
   *
   * <p>It retrieves optional button labels from the context or uses defaults:
   * <ul>
   *   <li>Confirm label defaults to "Ok"</li>
   *   <li>Cancel label defaults to null (no cancel button)</li>
   *   <li>Additional buttons default to an empty array</li>
   * </ul>
   *
   * @param ctx the dialog context containing the title, message, and optional button configuration
   * @return a UI node handle wrapping the created text dialog overlay
   * @throws IllegalArgumentException if the required title or message is not present in the context
   */
  public static UiHandle build(DialogContext ctx) {
    return build(ctx, false);
  }

  /**
   * Builds a UI node handle for a simple OK dialog using the shared text dialog overlay.
   *
   * <p>This builder keeps the OK dialog on the same rendering and input path as other text dialogs
   * while providing OK-specific defaults for title and confirm label.
   *
   * @param ctx the dialog context containing at least the message text
   * @return a UI node handle wrapping the created text dialog overlay
   * @throws IllegalArgumentException if the required message is not present in the context
   */
  public static UiHandle buildOk(DialogContext ctx) {
    return build(ctx, true);
  }

  private static UiHandle build(DialogContext ctx, boolean okDialog) {
    String title =
      okDialog
        ? ctx.find(DialogContextKeys.TITLE, String.class).orElse(DEFAULT_OK_TITLE)
        : ctx.require(DialogContextKeys.TITLE, String.class);
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String confirmLabel =
      ctx.find(DialogContextKeys.CONFIRM_LABEL, String.class)
        .orElse(okDialog ? DEFAULT_OK_CONFIRM_LABEL : DEFAULT_CONFIRM_LABEL);
    String cancelLabel = ctx.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse(null);
    String[] additionalButtons =
      ctx.find(DialogContextKeys.ADDITIONAL_BUTTONS, String[].class).orElse(new String[] {});

    return new OverlayHandle(
      new TextDialogOverlay(
        title, text, confirmLabel, cancelLabel, additionalButtons, ctx.dialogId()));
  }
}
