package level.devlevel.riddleHandler;

import contrib.components.SkillComponent;
import contrib.hud.DialogUtils;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.projectileSkill.BurningFireballSkill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import java.util.Map;

/**
 * The IllusionRiddleHandler class is used to handle the riddle of the illusion. The riddle consists
 * a series of rooms that the hero has to navigate through. The hero can run a certain amount of
 * laps inside a special room to receive a reward.
 */
public class IllusionRiddleHandler {

  private static final int LAP_REWARD = 3;
  private final DungeonLevel level;
  private final Point[][] initTeleporterSpawns;
  private final Point[][] lastTeleporterSpawns;
  private final Point[][] lapCheckpoints; // [location][3 tiles wide]
  private final Point riddleRewardSpawn; // The spawn point of the reward for solving the riddle
  private Point lastHeroPos = new Point(0, 0);
  private boolean rewardGiven = false;
  private int lapCounter = 0;
  private int lapProgress = 0;
  private int lastCheckpoint =
      -2; // Initialize with -2 to indicate no checkpoints have been passed yet
  private boolean thirdRoom = false;

  /**
   * Constructs a new IllusionRiddleHandler with the given custom points and level.
   *
   * @param namedPoints The custom points of the riddle room.
   * @param level The level of the riddle room.
   */
  public IllusionRiddleHandler(Map<String, Point> namedPoints, DungeonLevel level) {
    this.initTeleporterSpawns =
        new Point[][] {
          {level.getPoint(136), level.getPoint(137), level.getPoint(138)},
          {level.getPoint(139), level.getPoint(140), level.getPoint(141)}
        };
    this.lastTeleporterSpawns =
        new Point[][] {
          {level.getPoint(142), level.getPoint(143), level.getPoint(144)},
          {level.getPoint(145), level.getPoint(146), level.getPoint(147)}
        };
    this.lapCheckpoints =
        new Point[][] {
          {level.getPoint(148), level.getPoint(149), level.getPoint(150)}, // Right
          {level.getPoint(151), level.getPoint(152), level.getPoint(153)}, // Top
          {level.getPoint(154), level.getPoint(155), level.getPoint(156)}, // Left
          {level.getPoint(157), level.getPoint(158), level.getPoint(159)} // Bottom
        };
    this.riddleRewardSpawn = level.getPoint(160);

    this.level = level;
  }

  /** Handles the first tick of the riddle room. */
  public void onFirstTick() {
    level.tileAt(riddleRewardSpawn).ifPresent(tile -> tile.tintColor(0x22FF22FF));
  }

  /** Handles the tick logic of the riddle room. */
  public void onTick() {
    handleLapRoomLogic();
    handleRewardLogic();
  }

  private void handleRewardLogic() {
    if (rewardGiven) {
      return;
    }

    Coordinate heroPos = EntityUtils.getHeroCoordinate();
    if (riddleRewardSpawn.toCoordinate().equals(heroPos)) {
      giveReward();
    }
  }

  private void giveReward() {
    DialogUtils.showTextPopup(
        "You will receive enhanced perception as a reward for running "
            + LAP_REWARD
            + " laps!"
            + "You now can see and attack further than before.",
        "Run " + LAP_REWARD + " Laps");
    CameraSystem.camera().zoom += 0.1f;
    BurningFireballSkill burningFireballSkill =
        (BurningFireballSkill)
            (Game.hero()
                .orElseThrow()
                .fetch(SkillComponent.class)
                .orElseThrow()
                .getSkill(BurningFireballSkill.class)
                .orElseThrow());
    burningFireballSkill.range(burningFireballSkill.range() + 1f);
    this.rewardGiven = true;
    level.tileAt(riddleRewardSpawn).ifPresent(tile -> tile.tintColor(-1));
  }

  /**
   * Offsets (Teleports relative to the current position) the hero by the given offset.
   *
   * @param offset The offset to teleport the hero by.
   */
  private void offsetHero(Vector2 offset) {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return;
    }
    PositionComponent heroPc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    Point newPoint = heroPc.position().translate(offset);

    EntityUtils.teleportHeroTo(newPoint);
  }

  // Methods for Lap Room

  private void handleLapRoomLogic() {
    Point heroPos = EntityUtils.getHeroPosition();
    // Check if the hero has moved
    if (lastHeroPos == null || heroPos == null || lastHeroPos.equals(heroPos)) return;
    this.lastHeroPos = heroPos;

    handleLapProgressLogic(heroPos);

    if (thirdRoom && lapProgress == 0) {
      this.thirdRoom = false;
    }

    if (lapCounter == -1 && lapProgress == 3) {
      offsetHero(Vector2.of(-27, 0));
      this.lapCounter = 0;
      this.lapProgress = 0;
      this.lastCheckpoint = -2;
    } else if (!thirdRoom && lapCounter == LAP_REWARD && lapProgress == 1) {
      offsetHero(Vector2.of(24, 0));
      this.thirdRoom = true;
    } else {
      handleHiddenTeleporter(heroPos);
    }
  }

  /**
   * This method is called when the hero moves. It updates the lap progress and counter based on the
   * hero's movement through checkpoints.
   *
   * @param heroPos The current position of the hero.
   */
  private void handleLapProgressLogic(Point heroPos) {
    int currentCheckpoint = getCurrentCheckpoint(heroPos);
    // Check if the hero is not on a checkpoint or is on the same checkpoint as the last update
    if (currentCheckpoint == -1 || currentCheckpoint == lastCheckpoint) {
      return; // No update required
    }

    // Calculate the difference between the current and the last checkpoint
    int checkpointDifference = currentCheckpoint - lastCheckpoint;

    // Update lastCheckpoint for the next call
    this.lastCheckpoint = currentCheckpoint;

    // If this is the first checkpoint the hero reaches, just return
    if (lastCheckpoint == -2) {
      return;
    }

    // Moving forward through checkpoints
    if (checkpointDifference == 1 || checkpointDifference == -3) {
      this.lapProgress++;
      if (lapProgress > 3) {
        this.lapCounter++;
        this.lapProgress = 0;
      }
    }
    // Moving backward through checkpoints
    else if (checkpointDifference == -1 || checkpointDifference == 3) {
      this.lapProgress--;
      if (lapProgress < 0) {
        this.lapCounter--;
        this.lapProgress = 3;
      }
    }
  }

  /**
   * Needed for Lap Room.
   *
   * @param heroPos The current position of the hero.
   * @return The index of the checkpoint the hero is currently on. Returns -1 if the hero is not on
   *     a checkpoint.
   */
  private int getCurrentCheckpoint(Point heroPos) {
    for (int i = 0; i < lapCheckpoints.length; i++) {
      Point[] lapCheckpoint = lapCheckpoints[i];
      for (Point point : lapCheckpoint) {
        if (heroPos.toCoordinate().equals(point.toCoordinate())) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Handles the hidden teleporter inside the lap room.
   *
   * @param heroPos The current position of the hero.
   */
  private void handleHiddenTeleporter(Point heroPos) {
    for (Point initTeleporterPoint : initTeleporterSpawns[0]) { // start teleporter -> in
      if (heroPos.toCoordinate().equals(initTeleporterPoint.toCoordinate())) {
        offsetHero(Vector2.of(27, 0));
        return;
      }
    }
    for (Point lastTeleporterPoint : lastTeleporterSpawns[1]) { // end teleporter -> out
      if (heroPos.toCoordinate().equals(lastTeleporterPoint.toCoordinate())) {
        offsetHero(Vector2.of(-24, 0));
        this.lastCheckpoint = 0;
        this.lapProgress = 0;
        this.thirdRoom = false;
        return;
      }
    }
  }
}
