package contrib.hud.dialogs;

import core.ui.UiNodeHandle;
import core.ui.overlay.OverlayUiNodeHandle;

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
  public static UiNodeHandle build(DialogContext ctx) {
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
