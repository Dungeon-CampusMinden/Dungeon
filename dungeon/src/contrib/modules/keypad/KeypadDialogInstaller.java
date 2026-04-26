package contrib.modules.keypad;

import contrib.hud.dialogs.DialogRegistry;
import contrib.hud.dialogs.DialogType;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.modules.keypad.ui.KeypadDialogOverlay;
import core.ui.overlay.OverlayHandle;

/** Installs keypad-specific dialog bindings into the shared dialog registry. */
public final class KeypadDialogInstaller {
  private static boolean installed;

  private KeypadDialogInstaller() {}

  /** Installs the keypad dialog implementation one time. */
  public static synchronized void install() {
    if (installed) {
      return;
    }

    DialogRegistry.replace(
      DialogType.DefaultTypes.KEYPAD,
      context ->
        new OverlayHandle(new KeypadDialogOverlay(context.requireEntity(DialogContextKeys.ENTITY))));
    installed = true;
  }
}
