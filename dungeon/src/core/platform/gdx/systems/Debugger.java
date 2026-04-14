package core.platform.gdx.systems;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.debug.controls.DebugInputHandler;
import contrib.debug.controls.DebugPauseController;
import contrib.editor.level.systems.LevelEditorSystem;
import contrib.utils.components.ai.fight.AIChaseBehaviour;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.SelfDefendTransition;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.debug.DebugGameplayActions;
import core.level.Tile;
import core.platform.Platform;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;

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

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Debugger.class);

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
    LOGGER.info("Spawn Monster on Cursor");
    SPAWN_MONSTER(SkillTools.cursorPositionAsPoint());
  }

  /**
   * Spawn a monster at the given position if it is in the level and accessible.
   *
   * @param position The location to spawn the monster on.
   */
  public static void SPAWN_MONSTER(Point position) {
    Tile tile = null;
    try {
      tile = Game.tileAt(position).orElse(null);
    } catch (NullPointerException ex) {
      LOGGER.info(ex.getMessage());
    }

    if (tile != null && tile.isAccessible()) {
      Entity monster = new Entity("Debug Monster");

      monster.add(new PositionComponent(position));
      monster.add(new DrawComponent(new SimpleIPath("character/monster/chort")));
      monster.add(new VelocityComponent(1));
      monster.add(new HealthComponent());
      monster.add(new CollideComponent());
      monster.add(
        new AIComponent(
          new AIChaseBehaviour(1), new RadiusWalk(5, 1), new SelfDefendTransition()));

      Game.add(monster);
      LOGGER.info("Spawned monster at position {}", position);
    } else {
      LOGGER.info("Cannot spawn monster at non-existent or non-accessible tile");
    }
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
