package contrib.debug.controls;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import core.Entity;
import core.ui.UiHandle;
import core.utils.logging.DungeonLogger;

/**
 * Manages debugger pause state and one-frame advance behavior.
 *
 * <p>This class keeps the debugger pause flow separate from concrete key polling so the state can
 * be reused by different debug control entry points.
 */
public final class DebugPauseController {
  private static final DungeonLogger LOGGER =
    DungeonLogger.getLogger(DebugPauseController.class);

  private enum FrameAdvanceState {
    IDLE,
    SKIP_CURRENT_EXECUTION,
    PAUSE_AFTER_NEXT_EXECUTION
  }

  private Entity pauseMenu;
  private FrameAdvanceState frameAdvanceState = FrameAdvanceState.IDLE;

  /** Toggles the pause menu. */
  public void togglePause() {
    frameAdvanceState = FrameAdvanceState.IDLE;

    if (isPaused()) {
      unpause();
    } else {
      pause();
    }
  }

  /** Advances exactly one frame while the game is paused. */
  public void advanceFrame() {
    if (!isPaused()) {
      return;
    }

    unpause();
    frameAdvanceState = FrameAdvanceState.SKIP_CURRENT_EXECUTION;
    LOGGER.info("Advanced one frame");
  }

  /** Updates the internal frame-advance state machine. */
  public void updateFrameAdvance() {
    switch (frameAdvanceState) {
      case IDLE -> {
        // nothing to do
      }
      case SKIP_CURRENT_EXECUTION ->
        frameAdvanceState = FrameAdvanceState.PAUSE_AFTER_NEXT_EXECUTION;
      case PAUSE_AFTER_NEXT_EXECUTION -> {
        pause();
        frameAdvanceState = FrameAdvanceState.IDLE;
      }
    }
  }

  /**
   * Checks if the game is currently paused.
   *
   * <p>This method determines the pause state by verifying if the pause menu is present
   * and its associated UI dialog is attached to the UI handle.
   *
   * @return true if the game is currently paused, false otherwise
   */
  public boolean isPaused() {
    if (pauseMenu == null) {
      return false;
    }

    return pauseMenu.fetch(UIComponent.class)
      .flatMap(UIComponent::dialog)
      .map(UiHandle::isAttached)
      .orElse(false);
  }

  private void pause() {
    UIComponent ui =
      DialogFactory.show(
        DialogContext.builder().type(DialogType.DefaultTypes.PAUSE_MENU).center(false).build());

    ui.dialog().ifPresent(dialog -> dialog.setVisible(true));
    pauseMenu = ui.dialogContext().ownerEntity();
  }

  private void unpause() {
    if (pauseMenu == null) {
      return;
    }

    pauseMenu.fetch(UIComponent.class).ifPresent(UIUtils::closeDialog);
  }
}
