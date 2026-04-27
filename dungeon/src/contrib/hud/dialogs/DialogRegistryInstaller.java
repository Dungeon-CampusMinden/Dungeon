package contrib.hud.dialogs;

import contrib.hud.crafting.CraftingDialogBuilder;
import contrib.hud.dialogs.builders.FreeInputDialogBuilder;
import contrib.hud.dialogs.builders.TextDialogBuilder;
import contrib.hud.dialogs.overlays.PauseMenuDialogOverlay;
import contrib.hud.dialogs.overlays.YesNoDialogOverlay;
import contrib.hud.dialogs.showimage.ShowImageDialogBuilder;
import contrib.hud.elements.bars.AttributeBarLayout;
import contrib.hud.elements.bars.AttributeBarOverlay;
import contrib.hud.elements.bars.AttributeBarOverlayData;
import contrib.hud.inventory.DualInventoryDialogBuilder;
import contrib.hud.inventory.InventoryDialogBuilder;
import contrib.modules.keypad.ui.KeypadDialogOverlay;
import core.ui.overlay.OverlayHandle;

/**
 * Installs the available dialog backend implementations in the shared dialog registry.
 *
 * <p>The dialog registry itself only owns dialog creation. This class wires the default dialog
 * types to the currently available neutral dialog factories.
 */
public final class DialogRegistryInstaller {
  private static final String DEFAULT_YES_NO_TITLE = "Dialog";
  private static boolean initialized = false;

  private DialogRegistryInstaller() {}

  /**
   * Installs the default dialog types and their corresponding implementations into the dialog
   * registry.
   *
   * <p>This method ensures that all built-in dialog types defined in {@link
   * DialogType.DefaultTypes} are registered in the {@link DialogRegistry}.
   *
   * <p>Each dialog type is bound to its respective neutral dialog implementation.
   *
   * <p>If the installation process has already been completed, the method exits immediately without
   * performing any actions.
   *
   * <p>Note: This method is thread-safe and will only execute once, regardless of how many times it
   * is invoked.
   */
  public static synchronized void install() {
    if (initialized) {
      return;
    }

    DialogRegistry.replace(DialogType.DefaultTypes.TEXT, TextDialogBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.OK, TextDialogBuilder::buildOk);
    DialogRegistry.replace(
        DialogType.DefaultTypes.YES_NO,
        ctx ->
            new OverlayHandle(
                new YesNoDialogOverlay(
                    ctx.find(DialogContextKeys.TITLE, String.class).orElse(DEFAULT_YES_NO_TITLE),
                    ctx.require(DialogContextKeys.MESSAGE, String.class),
                    ctx.dialogId())));
    DialogRegistry.replace(DialogType.DefaultTypes.IMAGE, ShowImageDialogBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.FREE_INPUT, FreeInputDialogBuilder::build);
    DialogRegistry.replace(
        DialogType.DefaultTypes.ATTRIBUTE_BAR,
        ctx -> {
          AttributeBarOverlayData data =
              ctx.require(DialogContextKeys.ATTRIBUTE_BAR, AttributeBarOverlayData.class);
          AttributeBarOverlay overlay = new AttributeBarOverlay(data.styleName());
          AttributeBarLayout.updatePosition(overlay, data.pc(), data.verticalOffset());
          overlay.setVisible(true);
          return new OverlayHandle(overlay);
        });
    DialogRegistry.replace(
        DialogType.DefaultTypes.PAUSE_MENU, _ -> new OverlayHandle(new PauseMenuDialogOverlay()));
    DialogRegistry.replace(DialogType.DefaultTypes.INVENTORY, InventoryDialogBuilder::build);
    DialogRegistry.replace(
        DialogType.DefaultTypes.DUAL_INVENTORY, DualInventoryDialogBuilder::build);
    DialogRegistry.replace(DialogType.DefaultTypes.CRAFTING_GUI, CraftingDialogBuilder::build);
    DialogRegistry.replace(
        DialogType.DefaultTypes.KEYPAD,
        context ->
            new OverlayHandle(
                new KeypadDialogOverlay(context.requireEntity(DialogContextKeys.ENTITY))));

    initialized = true;
  }
}
