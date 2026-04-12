package core.platform.litiengine;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import contrib.hud.dialogs.LitiengineProgressBarDialogBuilder;
import contrib.hud.image.LitiengineShowImageDialogBuilder;
import contrib.modules.keypad.ui.LitiengineKeypadDialogBuilder;
import core.platform.litiengine.dialogs.*;
import core.ui.HeadlessUiNodeHandle;
import core.ui.UiNodeHandle;
import core.ui.dialogs.*;

/**
 * Registers a temporary LITIENGINE dialog backend.
 *
 * <p>At the current migration stage, this backend does not render actual dialogs yet.
 * Instead, it installs neutral fallback handles so dialog creation, lifecycle handling,
 * and callbacks can already work without depending on libGDX Scene2D.
 *
 * <p>The fallback registration is intentionally weak: real LITIENGINE dialog
 * implementations should be able to replace individual dialog types later without
 * changing this bootstrap.
 */
public final class LitiengineDialogFactoryBootstrap {
  private static boolean initialized = false;

  private LitiengineDialogFactoryBootstrap() {}

  public static synchronized void init() {
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

    // Real LITIENGINE dialog implementations available so far:
    DialogFactory.replace(DialogType.DefaultTypes.OK, LitiengineOkDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.YES_NO, LitiengineYesNoDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.TEXT, LitiengineTextDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.IMAGE, LitiengineShowImageDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.FREE_INPUT, LitiengineFreeInputDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.PROGRESS_BAR, LitiengineProgressBarDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.PAUSE_MENU, LitienginePauseMenuDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.KEYPAD, LitiengineKeypadDialogBuilder::build);
    DialogFactory.replace(DialogType.DefaultTypes.INVENTORY, LitiengineInventoryDialogBuilder::build);
    DialogFactory.replace(
      DialogType.DefaultTypes.DUAL_INVENTORY, LitiengineDualInventoryDialogBuilder::build);
    DialogFactory.replace(
      DialogType.DefaultTypes.CRAFTING_GUI, LitiengineCraftingDialogBuilder::build);

    initialized = true;
  }

  private static void registerFallback(DialogType type) {
    DialogFactory.registerIfAbsent(type, LitiengineDialogFactoryBootstrap::createFallbackHandle);
  }

  private static UiNodeHandle createFallbackHandle(DialogContext ctx) {
    return new HeadlessUiNodeHandle();
  }
}
