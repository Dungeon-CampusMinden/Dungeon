package contrib.debug.controls;

import contrib.configuration.KeyboardConfig;
import core.input.Keys;
import core.utils.InputManager;
import java.util.Objects;

/**
 * Centralized key-to-action dispatch for debugger input.
 *
 * <p>This keeps raw key polling out of concrete debugger systems and reduces the remaining
 * responsibilities of legacy debugger entry points.
 */
public final class DebugInputHandler {
  private DebugInputHandler() {}

  /** Handles one debugger input pass. */
  public static void handle(Actions actions) {
    Objects.requireNonNull(actions, "actions must not be null");

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_OUT.value())) {
      actions.zoomOut().run();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_IN.value())) {
      actions.zoomIn().run();
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_CURSOR.value())) {
      actions.teleportToCursor().run();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_END.value())) {
      actions.teleportToEnd().run();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_START.value())) {
      actions.teleportToStart().run();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_ON_END.value())) {
      actions.loadNextLevel().run();
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_SPAWN_MONSTER.value())) {
      actions.spawnMonsterOnCursor().run();
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_OPEN_DOORS.value())) {
      actions.openDoors().run();
    }

    if (InputManager.isKeyJustPressed(core.configuration.KeyboardConfig.TOGGLE_FULLSCREEN.value())
      && isShiftPressed()) {
      actions.toggleFullscreen().run();
    }

    if (InputManager.isKeyJustPressed(core.configuration.KeyboardConfig.PAUSE.value())) {
      actions.togglePause().run();
    }
    if (InputManager.isKeyJustPressed(core.configuration.KeyboardConfig.ADVANCE_FRAME.value())) {
      actions.advanceFrame().run();
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TOGGLE_HUD.value())) {
      actions.toggleDebugHud().run();
    }
  }

  private static boolean isShiftPressed() {
    return InputManager.isKeyPressed(Keys.SHIFT_LEFT)
      || InputManager.isKeyPressed(Keys.SHIFT_RIGHT);
  }

  /** Collection of debugger actions that can be bound to the standard debug keymap. */
  public record Actions(
    Runnable zoomOut,
    Runnable zoomIn,
    Runnable teleportToCursor,
    Runnable teleportToEnd,
    Runnable teleportToStart,
    Runnable loadNextLevel,
    Runnable spawnMonsterOnCursor,
    Runnable openDoors,
    Runnable toggleFullscreen,
    Runnable togglePause,
    Runnable advanceFrame,
    Runnable toggleDebugHud) {

    public Actions {
      Objects.requireNonNull(zoomOut, "zoomOut must not be null");
      Objects.requireNonNull(zoomIn, "zoomIn must not be null");
      Objects.requireNonNull(teleportToCursor, "teleportToCursor must not be null");
      Objects.requireNonNull(teleportToEnd, "teleportToEnd must not be null");
      Objects.requireNonNull(teleportToStart, "teleportToStart must not be null");
      Objects.requireNonNull(loadNextLevel, "loadNextLevel must not be null");
      Objects.requireNonNull(spawnMonsterOnCursor, "spawnMonsterOnCursor must not be null");
      Objects.requireNonNull(openDoors, "openDoors must not be null");
      Objects.requireNonNull(toggleFullscreen, "toggleFullscreen must not be null");
      Objects.requireNonNull(togglePause, "togglePause must not be null");
      Objects.requireNonNull(advanceFrame, "advanceFrame must not be null");
      Objects.requireNonNull(toggleDebugHud, "toggleDebugHud must not be null");
    }
  }
}
