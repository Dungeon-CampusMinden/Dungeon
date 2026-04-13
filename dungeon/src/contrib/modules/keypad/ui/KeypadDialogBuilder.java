package contrib.modules.keypad.ui;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.Entity;
import core.ui.overlay.OverlayUiNodeHandle;

/** Builds the LITIENGINE-backed keypad dialog. */
public final class KeypadDialogBuilder {

  private KeypadDialogBuilder() {}

  public static core.ui.UiNodeHandle build(DialogContext ctx) {
    Entity keypad = ctx.requireEntity(DialogContextKeys.ENTITY);
    return new OverlayUiNodeHandle(new KeypadDialogOverlay(keypad));
  }
}
