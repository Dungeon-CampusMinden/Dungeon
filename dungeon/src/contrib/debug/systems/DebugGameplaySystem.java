package contrib.debug.systems;

import contrib.debug.controls.DebugGameplayActions;
import contrib.debug.controls.DebugInputHandler;
import contrib.debug.controls.DebugPauseController;
import contrib.editor.level.LevelEditorSystem;
import core.Game;
import core.System;
import core.platform.Platform;
import core.utils.IVoidFunction;

/**
 * Debug system for gameplay-oriented runtime test actions.
 *
 * <p>This system provides debug actions such as camera zooming, teleporting, opening doors,
 * spawning test monsters, pausing, one-frame stepping, and toggling the debug HUD.
 *
 * <p>The system is backend-neutral. Concrete input polling is delegated to {@link
 * DebugInputHandler}, pause state is delegated to {@link DebugPauseController}, and gameplay
 * mutations are delegated to {@link DebugGameplayActions}.
 *
 * <p>Add this system to the game loop by adding the {@link #execute()} call in {@link
 * Game#userOnFrame(IVoidFunction)} or by registering it as a normal ECS system.
 */
public class DebugGameplaySystem extends System {

  private final DebugPauseController pauseController = new DebugPauseController();

  private final DebugInputHandler.Actions inputActions =
      new DebugInputHandler.Actions(
          new DebugInputHandler.CameraActions(
              () -> DebugGameplayActions.zoomCamera(-0.2f),
              () -> DebugGameplayActions.zoomCamera(0.2f)),
          new DebugInputHandler.TeleportActions(
              DebugGameplayActions::teleportToCursor,
              DebugGameplayActions::teleportToEndNeighbor,
              DebugGameplayActions::teleportToStart,
              DebugGameplayActions::teleportToExit),
          new DebugInputHandler.GameplayActions(
              () -> {
                if (!levelEditorActive()) {
                  spawnMonsterOnCursor();
                }
              },
              DebugGameplayActions::openDoors),
          new DebugInputHandler.DisplayActions(
              () -> Platform.window().toggleFullscreen(),
              () -> Platform.render().toggleDebugHud()),
          new DebugInputHandler.PauseActions(
              pauseController::togglePause, pauseController::advanceFrame));

  /** Creates a new debug gameplay system. */
  public DebugGameplaySystem() {
    super(AuthoritativeSide.CLIENT);
  }

  /** Spawns a debug monster at the cursor position. */
  public static void spawnMonsterOnCursor() {
    DebugGameplayActions.spawnMonsterAtCursor();
  }

  @Override
  public void stop() {
    // Debug systems are not stopped by gameplay state changes.
  }

  /** Checks for key input corresponding to debug gameplay actions. */
  public void execute() {
    DebugInputHandler.handle(inputActions);
    pauseController.updateFrameAdvance();
  }

  private static boolean levelEditorActive() {
    System system = Game.systems().get(LevelEditorSystem.class);

    if (system instanceof LevelEditorSystem levelEditorSystem) {
      return levelEditorSystem.active();
    }

    return false;
  }
}
