package core.platform.gdx.systems;

import contrib.debug.DebugMonsterSpawner;
import contrib.debug.controls.DebugInputHandler;
import contrib.debug.controls.DebugPauseController;
import contrib.editor.level.systems.LevelEditorSystem;
import core.Game;
import core.System;
import core.debug.DebugGameplayActions;
import core.level.Tile;
import core.platform.Platform;
import core.utils.IVoidFunction;
import core.utils.Point;

/**
 * Auxiliary class to accelerate the creation and testing of specific game scenarios.
 *
 * <p>It provides useful functionalities that can aid in verifying the correct behavior of a game
 * implementation.
 *
 * <p>Add the Debugger in the Game-Loop by adding the {@link #execute()} call in {@link
 * Game#userOnFrame(IVoidFunction)}
 */
public class Debugger extends System {

  private static final DebugPauseController PAUSE_CONTROLLER = new DebugPauseController();

  private static final DebugInputHandler.Actions INPUT_ACTIONS =
    new DebugInputHandler.Actions(
      () -> DebugGameplayActions.zoomCamera(-0.2f),
      () -> DebugGameplayActions.zoomCamera(0.2f),
      DebugGameplayActions::teleportToCursor,
      DebugGameplayActions::teleportToEndNeighbor,
      DebugGameplayActions::teleportToStart,
      DebugGameplayActions::loadNextLevel,
      () -> {
        if (!LevelEditorSystem.active()) {
          SPAWN_MONSTER_ON_CURSOR();
        }
      },
      DebugGameplayActions::openDoors,
      PAUSE_CONTROLLER::togglePause,
      PAUSE_CONTROLLER::advanceFrame,
      () -> Platform.render().toggleDebugHud());

  /** Creates a new Debugger system. */
  public Debugger() {
    super(AuthoritativeSide.CLIENT);
  }

  public static void ZOOM_CAMERA(float amount) {
    DebugGameplayActions.zoomCamera(amount);
  }

  /** Teleports the Player to the current position of the cursor. */
  public static void TELEPORT_TO_CURSOR() {
    DebugGameplayActions.teleportToCursor();
  }

  /** Teleports the Player to the end of the level, on a neighboring accessible tile if possible. */
  public static void TELEPORT_TO_END() {
    DebugGameplayActions.teleportToEndNeighbor();
  }

  /** Will teleport the Player on the EndTile so the next level gets loaded. */
  public static void LOAD_NEXT_LEVEL() {
    DebugGameplayActions.loadNextLevel();
  }

  /** Teleports the player to the start of the level. */
  public static void TELEPORT_TO_START() {
    DebugGameplayActions.teleportToStart();
  }

  /**
   * Teleports the player to the given tile.
   *
   * @param targetLocation the tile to teleport to
   */
  public static void TELEPORT(Tile targetLocation) {
    DebugGameplayActions.teleport(targetLocation);
  }

  /**
   * Teleports the player to the given location.
   *
   * @param targetLocation the location to teleport to
   */
  public static void TELEPORT(Point targetLocation) {
    DebugGameplayActions.teleport(targetLocation);
  }

  /** Spawns a monster at the cursor's position. */
  public static void SPAWN_MONSTER_ON_CURSOR() {
    DebugMonsterSpawner.spawnAtCursor();
  }

  /**
   * Spawn a monster at the given position if it is in the level and accessible.
   *
   * @param position The location to spawn the monster on.
   */
  public static void SPAWN_MONSTER(Point position) {
    DebugMonsterSpawner.spawnAt(position);
  }

  /** Pauses the game. */
  public static void PAUSE_GAME() {
    PAUSE_CONTROLLER.togglePause();
  }

  /** Advances one frame while paused. */
  public static void ADVANCE_FRAME() {
    PAUSE_CONTROLLER.advanceFrame();
  }

  @Override
  public void stop() {
    // Cant be stopped
  }

  /** Checks for key input corresponding to debugger functionalities. */
  public void execute() {
    DebugInputHandler.handle(INPUT_ACTIONS);
    PAUSE_CONTROLLER.updateFrameAdvance();
  }
}
