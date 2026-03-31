package core.platform.litiengine.dialogs;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.Entity;
import core.ui.UiNodeHandle;
import core.ui.litiengine.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed keypad dialog. */
public final class LitiengineKeypadDialogBuilder {

  private LitiengineKeypadDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    Entity keypad = ctx.requireEntity(DialogContextKeys.ENTITY);
    return new LitiengineUiNodeHandle(new LitiengineKeypadDialogOverlay(keypad));
  }
}
