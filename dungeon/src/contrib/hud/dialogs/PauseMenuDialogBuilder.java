package contrib.hud.dialogs;

import core.ui.overlay.OverlayUiNodeHandle;

/** Builds the LITIENGINE-backed pause menu dialog. */
public final class PauseMenuDialogBuilder {

  private PauseMenuDialogBuilder() {}

  public static core.ui.UiNodeHandle build(DialogContext ctx) {
    return new OverlayUiNodeHandle(new LitienginePauseMenuOverlay());
  }
}
