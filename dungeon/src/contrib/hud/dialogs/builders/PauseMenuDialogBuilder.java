package contrib.hud.dialogs.builders;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.PauseMenuOverlay;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;

/**
 * A builder for creating pause menu dialog UI nodes.
 *
 * <p>This utility class constructs UI node handles that display a pause menu overlay.
 *
 * <p>The pause menu provides options to control game flow such as resume, restart, or exit.
 */
public final class PauseMenuDialogBuilder {

  private PauseMenuDialogBuilder() {}

  /**
   * Builds a UI node handle for a pause menu overlay.
   *
   * @param ctx the dialog context (not used for pause menu configuration)
   * @return a UI node handle wrapping the created pause menu overlay
   */
  public static UiHandle build(DialogContext ctx) {
    return new OverlayHandle(new PauseMenuOverlay());
  }
}
