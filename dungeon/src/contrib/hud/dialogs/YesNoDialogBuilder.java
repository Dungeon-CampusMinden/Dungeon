package contrib.hud.dialogs;

import core.ui.UiNodeHandle;
import core.ui.overlay.OverlayUiNodeHandle;

/**
 * A builder for creating yes/no dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display a yes/no dialog overlay
 * with a message and two response buttons.
 *
 * <p>It retrieves the message text and optional title from the dialog context.
 */
public final class YesNoDialogBuilder {

  private YesNoDialogBuilder() {}

  /**
   * Builds a UI node handle for a yes/no dialog overlay.
   *
   * <p>This method requires the dialog context to contain a message text.
   *
   * <p>It retrieves an optional title from the context or uses a default title of "Dialog".
   *
   * @param ctx the dialog context containing the message and optional title
   * @return a UI node handle wrapping the created yes/no dialog overlay
   * @throws IllegalArgumentException if the required message is not present in the context
   */
  public static UiNodeHandle build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("Dialog");

    return new OverlayUiNodeHandle(
      new YesNoDialogOverlay(title, text, ctx.dialogId()));
  }
}
