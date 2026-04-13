package contrib.modules.keypad.ui;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.Entity;
import core.ui.UiNodeHandle;
import core.ui.overlay.LitiengineUiNodeHandle;

/** Builds the LITIENGINE-backed keypad dialog. */
public final class KeypadDialogBuilder {

  private KeypadDialogBuilder() {}

  public static UiNodeHandle build(DialogContext ctx) {
    Entity keypad = ctx.requireEntity(DialogContextKeys.ENTITY);
    return new LitiengineUiNodeHandle(new KeypadDialogOverlay(keypad));
  }
}
