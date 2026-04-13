package contrib.hud.dialogs;

import core.ui.UiNodeHandle;
import core.ui.overlay.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed pause menu dialog. */
public final class PauseMenuDialogBuilder {

  private PauseMenuDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    return new LitiengineUiNodeHandle(new LitienginePauseMenuOverlay());
  }
}
