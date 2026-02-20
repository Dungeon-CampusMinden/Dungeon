package contrib.utils.components;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.UIComponent;
import contrib.configuration.KeyboardConfig;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.PauseDialog;
import contrib.systems.DebugDrawSystem;
import contrib.systems.LevelEditorSystem;
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
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.systems.CameraSystem;
import core.utils.Direction;
import core.utils.IVoidFunction;
import core.utils.InputManager;
import core.utils.Point;
import core.utils.components.MissingComponentException;
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
 *
 * @see KeyboardConfig
 */
public class Debugger extends System {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Debugger.class);
  private static Entity pauseMenu;
  private static int advanceTimer = 0;

  /** Use this value to quickly test different states or values in any other part of the game. */
  public static int multiPurposeDebugValue = 0;

  /** Creates a new Debugger system. */
  public Debugger() {
    super(AuthoritativeSide.CLIENT);
  }

  /**
   * Zooms the camera in or out by the given amount.
   *
   * @param amount the length of the zoom change
   */
  public static void ZOOM_CAMERA(float amount) {
    LOGGER.debug("Change Camera Zoom {}", amount);
    CameraSystem.camera().zoom = Math.max(0.1f, CameraSystem.camera().zoom + amount);
    LOGGER.debug("New Camera Zoom {}", CameraSystem.camera().zoom);
  }

  /** Teleports the Player to the current position of the cursor. */
  public static void TELEPORT_TO_CURSOR() {
    LOGGER.info("TELEPORT TO CURSOR");
    TELEPORT(SkillTools.cursorPositionAsPoint());
  }

  /** Teleports the Player to the end of the level, on a neighboring accessible tile if possible. */
  public static void TELEPORT_TO_END() {
    LOGGER.info("TELEPORT TO END");

    Game.endTiles().stream()
        .findFirst()
        .ifPresent(
            end -> {
              Coordinate endTile = end.coordinate();
              Coordinate[] neighborTiles = {
                endTile.translate(Direction.UP),
                endTile.translate(Direction.DOWN),
                endTile.translate(Direction.LEFT),
                endTile.translate(Direction.RIGHT),
              };
              for (Coordinate neighborTile : neighborTiles) {
                Game.tileAt(neighborTile).ifPresent(Debugger::TELEPORT);
              }
            });
  }

  /** Will teleport the Player on the EndTile so the next level gets loaded. */
  public static void LOAD_NEXT_LEVEL() {
    LOGGER.info("TELEPORT ON END");
    Game.endTiles().stream().findFirst().ifPresent(Debugger::TELEPORT);
  }

  /** Teleports the player to the start of the level. */
  public static void TELEPORT_TO_START() {
    LOGGER.info("TELEPORT TO START");
    Game.startTile().ifPresent(Debugger::TELEPORT);
  }

  /**
   * Teleports the player to the given tile.
   *
   * @param targetLocation the tile to teleport to
   */
  public static void TELEPORT(Tile targetLocation) {
    TELEPORT(targetLocation.coordinate().toPoint());
  }

  /**
   * Teleports the player to the given location.
   *
   * @param targetLocation the location to teleport to
   */
  public static void TELEPORT(Point targetLocation) {
    Game.player()
        .ifPresent(
            player -> {
              PositionComponent pc =
                  player
                      .fetch(PositionComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(player, PositionComponent.class));

              // Attempt to teleport to targetLocation
              LOGGER.info("Attempting to teleport to {}", targetLocation);
              Tile t = Game.tileAt(targetLocation).orElse(null);
              if (t == null || !t.isAccessible()) {
                LOGGER.info("Cannot teleport to non-existing or non-accessible tile");
                return;
              }

              pc.position(targetLocation);
              LOGGER.info("Teleport successful");
            });
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
    // Get the tile at the given position
    Tile tile = null;
    try {
      tile = Game.tileAt(position).orElse(null);
    } catch (NullPointerException ex) {
      LOGGER.info(ex.getMessage());
    }

    // If the tile is accessible, spawn a monster at the position
    if (tile != null && tile.isAccessible()) {
      Entity monster = new Entity("Debug Monster");

      // Add components to the monster entity
      monster.add(new PositionComponent(position));
      monster.add(new DrawComponent(new SimpleIPath("character/monster/chort")));
      monster.add(new VelocityComponent(1));
      monster.add(new HealthComponent());
      monster.add(new CollideComponent());
      monster.add(
          new AIComponent(
              new AIChaseBehaviour(1), new RadiusWalk(5, 1), new SelfDefendTransition()));

      Game.add(monster);
      // Log that the monster was spawned
      LOGGER.info("Spawned monster at position " + position);
    } else {
      // Log that the monster couldn't be spawned
      LOGGER.info("Cannot spawn monster at non-existent or non-accessible tile");
    }
  }

  /** Pauses the game. */
  public static void PAUSE_GAME() {
    if (isPaused()) {
      unpause();
    } else {
      pause();
    }
  }

  private static void pause() {
    UIComponent ui = PauseDialog.showPauseDialog();
    pauseMenu = ui.dialogContext().ownerEntity();
  }

  private static void unpause() {
    if (pauseMenu == null) return;
    UIUtils.closeDialog(pauseMenu.fetch(UIComponent.class).orElseThrow());
  }

  private static boolean isPaused() {
    if (pauseMenu == null) return false;
    return pauseMenu.fetch(UIComponent.class).map(x -> x.dialog().getStage() != null).orElse(false);
  }

  private static void ADVANCE_FRAME() {
    if (!isPaused()) return;
    unpause();
    advanceTimer = 2; // Set to 2 to account for the current frame
    LOGGER.info("Advanced one frame");
  }

  private static void checkFrameAdvance() {
    if (advanceTimer > 0) {
      advanceTimer--;
      if (advanceTimer == 0) {
        pause();
      }
    }
  }

  private static void OPEN_DOORS() {
    Game.endTiles().forEach(ExitTile::open);
    Game.allTiles(LevelElement.DOOR).forEach(door -> ((DoorTile) door).open());
  }

  @Override
  public void stop() {
    // Cant be stopped
  }

  /**
   * Checks for key input corresponding to Debugger functionalities, and executes the relevant
   * function if detected.
   */
  public void execute() {
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_OUT.value()))
      Debugger.ZOOM_CAMERA(-0.2f);
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_IN.value()))
      Debugger.ZOOM_CAMERA(0.2f);

    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_CURSOR.value()))
      Debugger.TELEPORT_TO_CURSOR();
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_END.value()))
      Debugger.TELEPORT_TO_END();
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_START.value()))
      Debugger.TELEPORT_TO_START();
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_ON_END.value()))
      Debugger.LOAD_NEXT_LEVEL();
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_SPAWN_MONSTER.value())
        && !LevelEditorSystem.active()) Debugger.SPAWN_MONSTER_ON_CURSOR();
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_OPEN_DOORS.value()))
      Debugger.OPEN_DOORS();
    if (InputManager.isKeyJustPressed(core.configuration.KeyboardConfig.PAUSE.value()))
      Debugger.PAUSE_GAME();
    if (InputManager.isKeyJustPressed(core.configuration.KeyboardConfig.ADVANCE_FRAME.value()))
      Debugger.ADVANCE_FRAME();
    if (InputManager.isKeyJustPressed(KeyboardConfig.DEBUG_TOGGLE_HUD.value()))
      Game.system(DebugDrawSystem.class, DebugDrawSystem::toggleHUD);
    if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TOGGLE_SCENE_HUD.value()))
      Game.stage().ifPresent(stage -> stage.setDebugAll(!stage.isDebugAll()));
    if (InputHandler.isKeyJustPressed(KeyboardConfig.DEBUG_VALUE_UP.value())) {
      multiPurposeDebugValue += 1;
      LOGGER.info("multiPurposeDebugValue: " + multiPurposeDebugValue);
    }
    if (InputHandler.isKeyJustPressed(KeyboardConfig.DEBUG_VALUE_DOWN.value())) {
      multiPurposeDebugValue -= 1;
      LOGGER.info("multiPurposeDebugValue: " + multiPurposeDebugValue);
    }

    checkFrameAdvance();
  }
}
