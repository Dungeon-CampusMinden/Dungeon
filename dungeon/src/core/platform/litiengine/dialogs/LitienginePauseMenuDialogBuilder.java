package core.platform.litiengine.dialogs;

import contrib.hud.dialogs.DialogContext;
import core.ui.UiNodeHandle;
import core.ui.litiengine.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed pause menu dialog. */
public final class LitienginePauseMenuDialogBuilder {

  private LitienginePauseMenuDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    return new LitiengineUiNodeHandle(new LitienginePauseMenuOverlay());
  }
}
