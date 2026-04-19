package contrib.hud.dialogs.builders;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.overlays.OkDialogOverlay;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;

/**
 * A builder for creating OK dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display a simple OK dialog overlay
 * with a message and an OK button.
 *
 * <p>It retrieves the message text and optional title from the dialog context.
 */
public final class OkDialogBuilder {

  private OkDialogBuilder() {}

  /**
   * Builds a UI node handle for an OK dialog overlay.
   *
   * <p>This method requires the dialog context to contain a message text.
   *
   * <p>It retrieves an optional title from the context or uses a default title of "OK".
   *
   * @param ctx the dialog context containing the message and optional title
   * @return a UI node handle wrapping the created OK dialog overlay
   * @throws IllegalArgumentException if the required message is not present in the context
   */
  public static UiHandle build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("OK");

    return new OverlayHandle(new OkDialogOverlay(title, text, ctx.dialogId()));
  }
}
