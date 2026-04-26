package contrib.debug.controls;

import contrib.configuration.DebugKeyboardConfig;
import core.configuration.KeyboardConfig;
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
   * <p>This method processes various debug functionalities including zooming, teleportation,
   * spawning, toggling states, and frame advancement in the game environment.
   *
   * @param actions the collection of runnable debug actions to trigger in response to input events;
   *     must not be null
   * @throws NullPointerException if the provided actions parameter is null
   */
  public static void handle(Actions actions) {
    Objects.requireNonNull(actions, "actions must not be null");

    if (InputManager.isKeyJustPressed(DebugKeyboardConfig.DEBUG_ZOOM_OUT.value())) {
      actions.camera().zoomOut().run();
    }
    if (InputManager.isKeyJustPressed(DebugKeyboardConfig.DEBUG_ZOOM_IN.value())) {
      actions.camera().zoomIn().run();
    }

    if (InputManager.isKeyJustPressed(DebugKeyboardConfig.DEBUG_TELEPORT_TO_CURSOR.value())) {
      actions.teleport().toCursor().run();
    }
    if (InputManager.isKeyJustPressed(DebugKeyboardConfig.DEBUG_TELEPORT_TO_END.value())) {
      actions.teleport().toEnd().run();
    }
    if (InputManager.isKeyJustPressed(DebugKeyboardConfig.DEBUG_TELEPORT_TO_START.value())) {
      actions.teleport().toStart().run();
    }
    if (InputManager.isKeyJustPressed(DebugKeyboardConfig.DEBUG_TELEPORT_ON_END.value())) {
      actions.teleport().loadNextLevel().run();
    }

    if (InputManager.isKeyJustPressed(DebugKeyboardConfig.DEBUG_SPAWN_MONSTER.value())) {
      actions.gameplay().spawnMonsterOnCursor().run();
    }

    if (InputManager.isKeyJustPressed(DebugKeyboardConfig.DEBUG_OPEN_DOORS.value())) {
      actions.gameplay().openDoors().run();
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.TOGGLE_FULLSCREEN.value())
        && isShiftPressed()) {
      actions.display().toggleFullscreen().run();
    }

    if (InputManager.isKeyJustPressed(KeyboardConfig.PAUSE.value())) {
      actions.pause().togglePause().run();
    }
    if (InputManager.isKeyJustPressed(KeyboardConfig.ADVANCE_FRAME.value())) {
      actions.pause().advanceFrame().run();
    }

    if (InputManager.isKeyJustPressed(DebugKeyboardConfig.DEBUG_TOGGLE_HUD.value())) {
      actions.display().toggleDebugHud().run();
    }
  }

  private static boolean isShiftPressed() {
    return InputManager.isKeyPressed(Keys.SHIFT_LEFT)
        || InputManager.isKeyPressed(Keys.SHIFT_RIGHT);
  }

  /**
   * Represents debug actions grouped by concern to avoid a fragile positional constructor with
   * unrelated callbacks.
   *
   * @param camera Camera-related debug actions; must not be null.
   * @param teleport Teleport and level-transition actions; must not be null.
   * @param gameplay Gameplay mutation actions; must not be null.
   * @param display Display and HUD actions; must not be null.
   * @param pause Pause and frame-step actions; must not be null.
   */
  public record Actions(
      CameraActions camera,
      TeleportActions teleport,
      GameplayActions gameplay,
      DisplayActions display,
      PauseActions pause) {

    public Actions {
      Objects.requireNonNull(camera, "camera must not be null");
      Objects.requireNonNull(teleport, "teleport must not be null");
      Objects.requireNonNull(gameplay, "gameplay must not be null");
      Objects.requireNonNull(display, "display must not be null");
      Objects.requireNonNull(pause, "pause must not be null");
    }
  }

  /**
   * Represents actions that can be performed on a camera, such as zooming in and out.
   *
   * <p>Each action is defined as a {@link Runnable}, allowing the actions to be executed flexibly
   * when triggered. This class ensures that the provided actions are not null.
   *
   * @param zoomOut the action to zoom the camera out
   * @param zoomIn the action to zoom the camera in
   */
  public record CameraActions(Runnable zoomOut, Runnable zoomIn) {
    public CameraActions {
      Objects.requireNonNull(zoomOut, "zoomOut must not be null");
      Objects.requireNonNull(zoomIn, "zoomIn must not be null");
    }
  }

  /**
   * Represents actions related to teleportation and level transitions.
   *
   * <p>Each action is defined as a {@link Runnable}, allowing the actions to be executed flexibly
   * when triggered. This class ensures that the provided actions are not null.
   *
   * @param toCursor the action to teleport the player to the cursor position
   * @param toEnd the action to teleport the player to the end of the current level
   * @param toStart the action to teleport the player to the start of the current level
   * @param loadNextLevel the action to load and transition to the next level
   */
  public record TeleportActions(
      Runnable toCursor, Runnable toEnd, Runnable toStart, Runnable loadNextLevel) {
    public TeleportActions {
      Objects.requireNonNull(toCursor, "toCursor must not be null");
      Objects.requireNonNull(toEnd, "toEnd must not be null");
      Objects.requireNonNull(toStart, "toStart must not be null");
      Objects.requireNonNull(loadNextLevel, "loadNextLevel must not be null");
    }
  }

  /**
   * Represents gameplay-related debug actions, such as spawning enemies and opening doors.
   *
   * <p>Each action is defined as a {@link Runnable}, allowing the actions to be executed flexibly
   * when triggered. This class ensures that the provided actions are not null.
   *
   * @param spawnMonsterOnCursor the action to spawn a monster at the cursor position
   * @param openDoors the action to open all doors in the current level
   */
  public record GameplayActions(Runnable spawnMonsterOnCursor, Runnable openDoors) {
    public GameplayActions {
      Objects.requireNonNull(spawnMonsterOnCursor, "spawnMonsterOnCursor must not be null");
      Objects.requireNonNull(openDoors, "openDoors must not be null");
    }
  }

  /**
   * Represents display-related debug actions, such as toggling fullscreen and HUD visibility.
   *
   * <p>Each action is defined as a {@link Runnable}, allowing the actions to be executed flexibly
   * when triggered. This class ensures that the provided actions are not null.
   *
   * @param toggleFullscreen the action to toggle fullscreen mode on or off
   * @param toggleDebugHud the action to toggle the debug HUD visibility
   */
  public record DisplayActions(Runnable toggleFullscreen, Runnable toggleDebugHud) {
    public DisplayActions {
      Objects.requireNonNull(toggleFullscreen, "toggleFullscreen must not be null");
      Objects.requireNonNull(toggleDebugHud, "toggleDebugHud must not be null");
    }
  }

  /**
   * Represents pause-related debug actions, such as toggling pause and advancing the game frame by frame.
   *
   * <p>Each action is defined as a {@link Runnable}, allowing the actions to be executed flexibly
   * when triggered. This class ensures that the provided actions are not null.
   *
   * @param togglePause the action to toggle the pause state of the game
   * @param advanceFrame the action to advance the game by a single frame when paused
   */
  public record PauseActions(Runnable togglePause, Runnable advanceFrame) {
    public PauseActions {
      Objects.requireNonNull(togglePause, "togglePause must not be null");
      Objects.requireNonNull(advanceFrame, "advanceFrame must not be null");
    }
  }
}
