package level.produs;

import client.Client;
import coderunner.BlocklyCommands;
import contrib.hud.DialogUtils;
import contrib.systems.EventScheduler;
import contrib.utils.IAction;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.PitTile;
import core.level.loader.DungeonLoader;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.MissingHeroException;
import core.utils.components.MissingComponentException;
import entities.monster.BlocklyMonster;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This is the first level of the 3-stage boss fight. It features a Red-Light Green-Light mechanic:
 * the player may only move when the boss is not looking in the direction of the hero, using the
 * wait block to time movements.
 */
public class Level020 extends BlocklyLevel {
  /** Time in milliseconds it takes for a pit to fully open after being triggered. */
  private static final int PIT_TIME_TO_OPEN_IN_MS = 120;

  /** Duration in milliseconds for each turn or decision cycle in the game logic. */
  private static final int TURN_TIMER_IN_MS = 1500;

  /**
   * Grace period in milliseconds during which the player can still move after the boss has time
   * ("coyote time" mechanic).
   */
  private static final int COYOTE_TIME_IN_MS = 500;

  /** Focus point for the camera in this level. */
  private static final Coordinate CAMERA_POINT = new Coordinate(15, 8);

  /** Minimum distance (in tiles) between hero and boss at which the boss escapes. */
  private static final int ESCAPE_DISTANCE = 2;

  private static boolean showText = true;

  /**
   * Helper flag for the EventScheduler; set to true if the boss is looking left and the coyote time
   * has expired.
   */
  private boolean executeCheck = true;

  private PositionComponent heropc;
  private VelocityComponent herovc;

  private Entity boss;
  private PositionComponent bosspc;
  private IAction turnAction =
      () -> {
        if (bosspc.viewDirection() == Direction.LEFT) {
          BlocklyCommands.turnEntity(boss, Direction.RIGHT);
          executeCheck = false;
        } else {
          BlocklyCommands.turnEntity(boss, Direction.LEFT);
          EventScheduler.scheduleAction(() -> executeCheck = true, COYOTE_TIME_IN_MS);
        }
      };

  private EventScheduler.ScheduledAction scheduledAction;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level020(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 20");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Variable
        "get_number",
        // Kategorien
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusOn(CAMERA_POINT);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.heroViewDirection(Direction.RIGHT);
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    heropc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    herovc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));

    ((DoorTile) Game.randomTile(LevelElement.DOOR).orElseThrow()).close();

    boss =
        BlocklyMonster.BLACK_KNIGHT
            .builder()
            .attackRange(0)
            .addToGame()
            .viewDirection(Direction.RIGHT)
            .build(
                Game.randomTile(LevelElement.EXIT)
                    .orElseThrow()
                    .coordinate()
                    .translate(Direction.LEFT));
    bosspc =
        boss.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(boss, PositionComponent.class));
    Game.allTiles(LevelElement.PIT)
        .forEach(
            tile -> {
              ((PitTile) tile).timeToOpen(PIT_TIME_TO_OPEN_IN_MS);
              ((PitTile) tile).close();
            });

    if (showText) {
      DialogUtils.showTextPopup(
          "Von dir lass ich mich nicht besiegen! Meine Waffe erkennt jede Bewegung und vernichtet dich auf der Stelle!",
          "BOSS");
      showText = false;
    }
  }

  @Override
  protected void onTick() {
    if (boss == null) return;
    // rotate boss after duration
    if (!EventScheduler.isScheduled(scheduledAction))
      scheduledAction = EventScheduler.scheduleAction(turnAction, TURN_TIMER_IN_MS);
    if (executeCheck) redLightGreenLight();
    checkEscapeDistance();
  }

  /**
   * Checks if the player is moving while the boss is watching.
   *
   * <p>If the player moves while the boss is looking (left), the game ends. A small area at the
   * beginning is considered a safe zone and is excluded from the check.
   */
  private void redLightGreenLight() {
    float x = heropc.position().x();
    float y = heropc.position().y();

    // The small area at the beginning is a safe zone
    boolean inSafeZone = x <= 6 || (x <= 3 && y >= 6 && y <= 8);
    if (!inSafeZone) {
      if (bosspc.viewDirection() == Direction.LEFT) {
        if (herovc.currentVelocity().length() > 0) {
          DialogUtils.showTextPopup("HAB ICH DICH!", "GAME OVER!", Client::restart);
        }
      }
    }
  }

  /**
   * Checks whether the hero has reached the escape threshold distance to the boss.
   *
   * <p>If the threshold is reached, the boss will taunt the hero, be removed from the game, and all
   * scheduled actions will be cleared.
   */
  private void checkEscapeDistance() {
    float heroX = heropc.position().x();
    float bossX = bosspc.position().x();

    // If the hero gets close enough to the boss, the boss escapes
    if (heroX >= bossX - ESCAPE_DISTANCE) {
      Game.remove(boss);
      boss = null;
      EventScheduler.clear();
      DialogUtils.showTextPopup("Mich kriegst du nie!", "BOSS", DungeonLoader::loadNextLevel);
    }
  }
}
