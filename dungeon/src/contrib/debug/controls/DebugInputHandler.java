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

  /**
   * Handles debug-related input actions by mapping key presses to their respective operations
   * defined in the provided {@link Actions} record.
   *
   * <p>This method processes various debug functionalities including zooming, teleportation, spawning,
   * toggling states, and frame advancement in the game environment.
   *
   * @param actions the collection of runnable debug actions to trigger in response to input events;
   *                must not be null
   * @throws NullPointerException if the provided actions parameter is null
   */
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

  /**
   * Represents a collection of runnable actions used to handle debug-related input events
   * in the game environment.
   *
   * <p>Each action corresponds to a specific operation that can be triggered by key events,
   * including zooming, teleportation, state toggling, spawning entities, and frame advancement.
   *
   * <p>All actions are mandatory and must not be null when creating an instance of this record.
   *
   * @param zoomOut           Action for zooming out; must not be null.
   * @param zoomIn            Action for zooming in; must not be null.
   * @param teleportToCursor  Action for teleporting to the cursor; must not be null.
   * @param teleportToEnd     Action for teleporting to the end of the level; must not be null.
   * @param teleportToStart   Action for teleporting to the start of the level; must not be null.
   * @param loadNextLevel     Action for loading the next level; must not be null.
   * @param spawnMonsterOnCursor Action for spawning a monster at the cursor position; must not be null.
   * @param openDoors         Action for opening all doors; must not be null.
   * @param toggleFullscreen  Action for toggling fullscreen mode; must not be null.
   * @param togglePause       Action for toggling the game's pause state; must not be null.
   * @param advanceFrame      Action for advancing a single frame while the game is paused; must not be null.
   * @param toggleDebugHud    Action for toggling the visibility of the debug HUD; must not be null.
   */
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
