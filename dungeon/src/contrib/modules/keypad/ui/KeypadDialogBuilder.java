package contrib.modules.keypad.ui;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.ui.UiHandle;
import core.ui.overlay.OverlayHandle;

/** Builds the keypad dialog overlay for keypad module dialog contexts. */
public final class KeypadDialogBuilder {

  private KeypadDialogBuilder() {}

  /**
   * Creates a keypad dialog overlay from the given dialog context.
   *
   * @param context dialog context containing the keypad entity
   * @return UI handle wrapping the keypad overlay
   */
  public static UiHandle build(DialogContext context) {
    return new OverlayHandle(
        new KeypadDialogOverlay(context.requireEntity(DialogContextKeys.ENTITY)));
  }
}
