package contrib.hud.dialogs;

import contrib.hud.crafting.CraftingDialogBuilder;
import contrib.hud.image.ShowImageDialogBuilder;
import contrib.hud.inventory.DualInventoryDialogBuilder;
import contrib.hud.inventory.InventoryDialogBuilder;
import contrib.modules.keypad.ui.KeypadDialogBuilder;
import core.ui.HeadlessUiNodeHandle;
import core.ui.UiNodeHandle;

/**
 * Installs the available dialog backend implementations in the shared dialog factory.
 *
 * <p>The dialog factory itself only owns the registry. This class wires the default dialog types to
 * the currently available neutral dialog builders and installs fallback handles for dialog types
 * that do not have a concrete visual implementation yet.
 */
public final class DialogBackendInstaller {
  private static boolean initialized = false;

  private DialogBackendInstaller() {}

  public static synchronized void install() {
    if (initialized) {
      return;
    }

    registerFallback(DialogType.DefaultTypes.OK);
    registerFallback(DialogType.DefaultTypes.YES_NO);
    registerFallback(DialogType.DefaultTypes.TEXT);
    registerFallback(DialogType.DefaultTypes.IMAGE);
    registerFallback(DialogType.DefaultTypes.FREE_INPUT);
    registerFallback(DialogType.DefaultTypes.INVENTORY);
    registerFallback(DialogType.DefaultTypes.DUAL_INVENTORY);
    registerFallback(DialogType.DefaultTypes.CRAFTING_GUI);
    registerFallback(DialogType.DefaultTypes.KEYPAD);
    registerFallback(DialogType.DefaultTypes.PROGRESS_BAR);
    registerFallback(DialogType.DefaultTypes.PAUSE_MENU);

    DialogFactory.replace(DialogType.DefaultTypes.OK, OkDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.YES_NO, YesNoDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.TEXT, TextDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.IMAGE, ShowImageDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.FREE_INPUT, FreeInputDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.PROGRESS_BAR, ProgressBarDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.PAUSE_MENU, PauseMenuDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.KEYPAD, KeypadDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.INVENTORY, InventoryDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.DUAL_INVENTORY, DualInventoryDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.CRAFTING_GUI, CraftingDialogBuilder::build);

    initialized = true;
  }

  private static void registerFallback(DialogType type) {
    DialogFactory.registerIfAbsent(type, DialogBackendInstaller::createFallbackHandle);
  }

  private static UiNodeHandle createFallbackHandle(DialogContext ctx) {
    return new HeadlessUiNodeHandle();
  }
}
