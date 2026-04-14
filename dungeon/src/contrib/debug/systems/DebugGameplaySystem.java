package contrib.debug.systems;

import contrib.debug.DebugMonsterSpawner;
import contrib.debug.controls.DebugInputHandler;
import contrib.debug.controls.DebugPauseController;
import contrib.editor.level.LevelEditorSystem;
import core.Game;
import core.System;
import core.debug.DebugGameplayActions;
import core.level.Tile;
import core.platform.Platform;
import core.utils.IVoidFunction;
import core.utils.Point;

/**
 * Debug system for gameplay-oriented runtime test actions.
 *
 * <p>This system provides debug actions such as camera zooming, teleporting, opening doors,
 * spawning test monsters, pausing, one-frame stepping, and toggling the debug HUD.
 *
 * <p>The system is backend-neutral. Concrete input polling is delegated to {@link
 * DebugInputHandler}, pause state is delegated to {@link DebugPauseController}, and gameplay
 * mutations are delegated to {@link DebugGameplayActions} or {@link DebugMonsterSpawner}.
 *
 * <p>Add this system to the game loop by adding the {@link #execute()} call in {@link
 * Game#userOnFrame(IVoidFunction)} or by registering it as a normal ECS system.
 */
public class DebugGameplaySystem extends System {

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
        if (!levelEditorActive()) {
          spawnMonsterOnCursor();
        }
      },
      DebugGameplayActions::openDoors,
      PAUSE_CONTROLLER::togglePause,
      PAUSE_CONTROLLER::advanceFrame,
      () -> Platform.render().toggleDebugHud());

  /** Creates a new debug gameplay system. */
  public DebugGameplaySystem() {
    super(AuthoritativeSide.CLIENT);
  }

  public static void zoomCamera(float amount) {
    DebugGameplayActions.zoomCamera(amount);
  }

  /** Teleports the player to the current cursor position. */
  public static void teleportToCursor() {
    DebugGameplayActions.teleportToCursor();
  }

  /** Teleports the player next to the level end if possible. */
  public static void teleportToEnd() {
    DebugGameplayActions.teleportToEndNeighbor();
  }

  /** Teleports the player onto the end tile so the next level can be loaded. */
  public static void loadNextLevel() {
    DebugGameplayActions.loadNextLevel();
  }

  /** Teleports the player to the level start. */
  public static void teleportToStart() {
    DebugGameplayActions.teleportToStart();
  }

  public static void teleport(Tile targetLocation) {
    DebugGameplayActions.teleport(targetLocation);
  }

  public static void teleport(Point targetLocation) {
    DebugGameplayActions.teleport(targetLocation);
  }

  /** Spawns a debug monster at the cursor position. */
  public static void spawnMonsterOnCursor() {
    DebugMonsterSpawner.spawnAtCursor();
  }

  /** Spawns a debug monster at the given position. */
  public static void spawnMonster(Point position) {
    DebugMonsterSpawner.spawnAt(position);
  }

  /** Toggles the debug pause menu. */
  public static void pauseGame() {
    PAUSE_CONTROLLER.togglePause();
  }

  /** Advances one frame while paused. */
  public static void advanceFrame() {
    PAUSE_CONTROLLER.advanceFrame();
  }

  @Override
  public void stop() {
    // Debug systems are not stopped by gameplay state changes.
  }

  /** Checks for key input corresponding to debug gameplay actions. */
  public void execute() {
    DebugInputHandler.handle(INPUT_ACTIONS);
    PAUSE_CONTROLLER.updateFrameAdvance();
  }

  private static boolean levelEditorActive() {
    System system = Game.systems().get(LevelEditorSystem.class);

    if (system instanceof LevelEditorSystem levelEditorSystem) {
      return levelEditorSystem.active();
    }

    return false;
  }
}
