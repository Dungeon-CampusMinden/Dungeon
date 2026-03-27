package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import core.ui.UiNodeHandle;
import core.ui.gdx.GdxUiNodeHandle;

/**
 * Registers the built-in libGDX-backed dialog implementations.
 *
 * <p>This keeps the concrete Scene2D dialog wiring out of {@link DialogFactory}.
 */
public final class GdxDialogFactoryBootstrap {
  private static boolean initialized = false;

  private GdxDialogFactoryBootstrap() {}

  public static synchronized void init() {
    if (initialized) {
      return;
    }

    DialogFactory.register(
      DialogType.DefaultTypes.OK,
      ctx -> wrap(GdxOkDialogBuilder.build(ctx)));
    DialogFactory.register(
      DialogType.DefaultTypes.YES_NO,
      ctx -> wrap(GdxYesNoDialogBuilder.build(ctx)));
    DialogFactory.register(DialogType.DefaultTypes.TEXT, ctx -> wrap(TextDialog.build(ctx)));
    DialogFactory.register(
      DialogType.DefaultTypes.IMAGE, ctx -> wrap(GdxShowImageDialogBuilder.build(ctx)));
    DialogFactory.register(
      DialogType.DefaultTypes.FREE_INPUT,
      ctx -> wrap(GdxFreeInputDialogBuilder.build(ctx)));
    DialogFactory.register(
      DialogType.DefaultTypes.INVENTORY,
      ctx -> wrap(GdxInventoryDialogBuilder.buildSimple(ctx)));
    DialogFactory.register(
      DialogType.DefaultTypes.DUAL_INVENTORY,
      ctx -> wrap(GdxInventoryDialogBuilder.buildDual(ctx)));
    DialogFactory.register(
      DialogType.DefaultTypes.CRAFTING_GUI,
      ctx -> wrap(GdxCraftingDialogBuilder.build(ctx)));
    DialogFactory.register(
      DialogType.DefaultTypes.KEYPAD,
      ctx -> wrap(GdxKeypadDialogBuilder.build(ctx)));
    DialogFactory.register(
      DialogType.DefaultTypes.PROGRESS_BAR,
      ctx -> wrap(GdxProgressBarDialogBuilder.build(ctx)));
    DialogFactory.register(
      DialogType.DefaultTypes.PAUSE_MENU,
      ctx -> wrap(GdxPauseMenuDialogBuilder.build(ctx)));

    initialized = true;
  }

  private static UiNodeHandle wrap(Group group) {
    return new GdxUiNodeHandle(group);
  }
}
