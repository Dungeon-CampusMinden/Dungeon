package contrib.platform.gdx.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import core.ui.UiNodeHandle;
import core.ui.gdx.GdxUiNodeHandle;

/**
 * Registers the built-in libGDX-backed dialog implementations.
 *
 * <p>This keeps the concrete Scene2D dialog wiring out of {@link DialogFactory}.
 *
 * <p>Inventory-related dialogs are intentionally no longer wired here. Their active migration path
 * now lives in the LITIENGINE backend, and the remaining Scene2D inventory implementation should
 * not stay on the active dialog bootstrap path.
 */
public final class GdxDialogFactoryBootstrap {
  private static boolean initialized = false;

  private GdxDialogFactoryBootstrap() {}

  public static synchronized void init() {
    if (initialized) {
      return;
    }

    DialogFactory.replace(DialogType.DefaultTypes.OK, ctx -> wrap(GdxOkDialogBuilder.build(ctx)));
    DialogFactory.replace(
      DialogType.DefaultTypes.YES_NO, ctx -> wrap(GdxYesNoDialogBuilder.build(ctx)));
    DialogFactory.replace(
      DialogType.DefaultTypes.TEXT, ctx -> wrap(GdxTextDialogBuilder.build(ctx)));
    DialogFactory.replace(
      DialogType.DefaultTypes.IMAGE, ctx -> wrap(GdxShowImageDialogBuilder.build(ctx)));
    DialogFactory.replace(
      DialogType.DefaultTypes.FREE_INPUT, ctx -> wrap(GdxFreeInputDialogBuilder.build(ctx)));
    DialogFactory.replace(
      DialogType.DefaultTypes.KEYPAD, ctx -> wrap(GdxKeypadDialogBuilder.build(ctx)));
    DialogFactory.replace(
      DialogType.DefaultTypes.PROGRESS_BAR,
      ctx -> wrap(GdxProgressBarDialogBuilder.build(ctx)));
    DialogFactory.replace(
      DialogType.DefaultTypes.PAUSE_MENU, ctx -> wrap(GdxPauseMenuDialogBuilder.build(ctx)));

    initialized = true;
  }

  private static UiNodeHandle wrap(Group group) {
    return new GdxUiNodeHandle(group);
  }
}
