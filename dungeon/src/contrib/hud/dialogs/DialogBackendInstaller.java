package contrib.hud.dialogs;

import contrib.hud.crafting.CraftingDialogBuilder;
import contrib.hud.dialogs.builders.*;
import contrib.hud.showimage.ShowImageDialogBuilder;
import contrib.hud.inventory.DualInventoryDialogBuilder;
import contrib.hud.inventory.InventoryDialogBuilder;
import contrib.modules.keypad.ui.KeypadDialogBuilder;
import core.ui.NullUiHandle;
import core.ui.UiHandle;

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

  /**
   * Installs the default dialog types and their corresponding implementations into the dialog factory.
   *
   * <p>This method ensures that all built-in dialog types defined in {@link DialogType.DefaultTypes}
   * are registered in the {@link DialogFactory}.
   *
   * <p>Fallback handlers are also set up for types without visual implementations.
   * Additionally, it binds each dialog type to its respective neutral builder.
   *
   * <p>If the installation process has already been completed, the method exits immediately
   * without performing any actions.
   *
   * <p>The registration process includes:
   * <ul>
   *   <li>Registering fallback handlers for all built-in dialog types.</li>
   *   <li>Replacing factory entries for specific dialog types with their corresponding builder methods.</li>
   * </ul>
   *
   * <p>Note: This method is thread-safe and will only execute once, regardless of how many
   * times it is invoked.
   */
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

  private static UiHandle createFallbackHandle(DialogContext ctx) {
    return new NullUiHandle();
  }
}
