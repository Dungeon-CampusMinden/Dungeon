package contrib.hud.dialogs;

import contrib.hud.crafting.CraftingDialogBuilder;
import contrib.hud.dialogs.builders.*;
import contrib.hud.elements.bars.AttributeBarOverlayBuilder;
import contrib.hud.dialogs.showimage.ShowImageDialogBuilder;
import contrib.hud.inventory.DualInventoryDialogBuilder;
import contrib.hud.inventory.InventoryDialogBuilder;

/**
 * Installs the available dialog backend implementations in the shared dialog registry.
 *
 * <p>The dialog registry itself only owns dialog creation. This class wires the default dialog
 * types to the currently available neutral dialog builders.
 */
public final class DialogRegistryInstaller {
  private static boolean initialized = false;

  private DialogRegistryInstaller() {}

  /**
   * Installs the default dialog types and their corresponding implementations into the dialog
   * registry.
   *
   * <p>This method ensures that all built-in dialog types defined in {@link DialogType.DefaultTypes}
   * are registered in the {@link DialogRegistry}.
   *
   * <p>Each dialog type is bound to its respective neutral builder implementation.
   *
   * <p>If the installation process has already been completed, the method exits immediately
   * without performing any actions.
   *
   * <p>Note: This method is thread-safe and will only execute once, regardless of how many
   * times it is invoked.
   */
  public static synchronized void install() {
    if (initialized) {
      return;
    }

    DialogRegistry.replace(DialogType.DefaultTypes.TEXT, TextDialogBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.OK, TextDialogBuilder::buildOk);
    DialogRegistry.replace(DialogType.DefaultTypes.YES_NO, YesNoDialogBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.IMAGE, ShowImageDialogBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.FREE_INPUT, FreeInputDialogBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.ATTRIBUTE_BAR, AttributeBarOverlayBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.PAUSE_MENU, PauseMenuDialogBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.INVENTORY, InventoryDialogBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.DUAL_INVENTORY, DualInventoryDialogBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.CRAFTING_GUI, CraftingDialogBuilder::build);

    initialized = true;
  }
}
