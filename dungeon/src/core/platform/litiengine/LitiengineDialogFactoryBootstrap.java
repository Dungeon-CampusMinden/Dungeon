package core.platform.litiengine;

import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import core.ui.HeadlessUiNodeHandle;
import core.ui.UiNodeHandle;

/**
 * Registers a temporary LITIENGINE dialog backend.
 *
 * <p>At the current migration stage, this backend does not render actual dialogs yet.
 * Instead, it installs neutral fallback handles so dialog creation, lifecycle handling,
 * and callbacks can already work without depending on libGDX Scene2D.
 */
public final class LitiengineDialogFactoryBootstrap {
  private static boolean initialized = false;

  private LitiengineDialogFactoryBootstrap() {}

  public static synchronized void init() {
    if (initialized) {
      return;
    }

    register(DialogType.DefaultTypes.OK);
    register(DialogType.DefaultTypes.YES_NO);
    register(DialogType.DefaultTypes.TEXT);
    register(DialogType.DefaultTypes.IMAGE);
    register(DialogType.DefaultTypes.FREE_INPUT);
    register(DialogType.DefaultTypes.INVENTORY);
    register(DialogType.DefaultTypes.DUAL_INVENTORY);
    register(DialogType.DefaultTypes.CRAFTING_GUI);
    register(DialogType.DefaultTypes.KEYPAD);
    register(DialogType.DefaultTypes.PROGRESS_BAR);
    register(DialogType.DefaultTypes.PAUSE_MENU);

    initialized = true;
  }

  private static void register(DialogType type) {
    DialogFactory.register(type, LitiengineDialogFactoryBootstrap::createFallbackHandle);
  }

  private static UiNodeHandle createFallbackHandle(DialogContext ctx) {
    return new HeadlessUiNodeHandle();
  }
}
