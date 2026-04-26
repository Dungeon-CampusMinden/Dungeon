package contrib.modules.keypad;

import contrib.hud.dialogs.DialogRegistry;
import contrib.hud.dialogs.DialogType;
import contrib.modules.keypad.ui.KeypadDialogBuilder;

/** Installs keypad-specific dialog bindings into the shared dialog registry. */
public final class KeypadDialogInstaller {
  private static boolean installed;

  private KeypadDialogInstaller() {}

  /** Installs the keypad dialog implementation one time. */
  public static synchronized void install() {
    if (installed) {
      return;
    }

    DialogRegistry.replace(DialogType.DefaultTypes.KEYPAD, KeypadDialogBuilder::build);
    installed = true;
  }
}
