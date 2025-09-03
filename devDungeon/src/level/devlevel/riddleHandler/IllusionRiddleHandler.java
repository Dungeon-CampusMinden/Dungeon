package level.devlevel.riddleHandler;

import contrib.entities.HeroFactory;
import contrib.hud.DialogUtils;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import entities.BurningFireballSkill;
import java.util.List;

/**
 * The IllusionRiddleHandler class is used to handle the riddle of the illusion. The riddle consists
 * a series of rooms that the hero has to navigate through. The hero can run a certain amount of
 * laps inside a special room to receive a reward.
 */
public class IllusionRiddleHandler {

  private static final int LAP_REWARD = 3;
  private final DungeonLevel level;
  private final Coordinate[][] initTeleporterSpawns;
  private final Coordinate[][] lastTeleporterSpawns;
  private final Coordinate[][] lapCheckpoints; // [location][3 tiles wide]
  private final Coordinate
      riddleRewardSpawn; // The spawn point of the reward for solving the riddle
  private Coordinate lastHeroPos = new Coordinate(0, 0);
  private boolean rewardGiven = false;
  private int lapCounter = 0;
  private int lapProgress = 0;
  private int lastCheckpoint =
      -2; // Initialize with -2 to indicate no checkpoints have been passed yet
  private boolean thirdRoom = false;

  /**
   * Constructs a new IllusionRiddleHandler with the given custom points and level.
   *
   * @param customPoints The custom points of the riddle room.
   * @param level The level of the riddle room.
   */
  public IllusionRiddleHandler(List<Coordinate> customPoints, DungeonLevel level) {
    this.initTeleporterSpawns =
        new Coordinate[][] {
          {customPoints.get(136), customPoints.get(137), customPoints.get(138)},
          {customPoints.get(139), customPoints.get(140), customPoints.get(141)}
        };
    this.lastTeleporterSpawns =
        new Coordinate[][] {
          {customPoints.get(142), customPoints.get(143), customPoints.get(144)},
          {customPoints.get(145), customPoints.get(146), customPoints.get(147)}
        };
    this.lapCheckpoints =
        new Coordinate[][] {
          {customPoints.get(148), customPoints.get(149), customPoints.get(150)}, // Right
          {customPoints.get(151), customPoints.get(152), customPoints.get(153)}, // Top
          {customPoints.get(154), customPoints.get(155), customPoints.get(156)}, // Left
          {customPoints.get(157), customPoints.get(158), customPoints.get(159)} // Bottom
        };
    this.riddleRewardSpawn = customPoints.get(160);

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
    if (riddleRewardSpawn.equals(heroPos)) {
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
    BurningFireballSkill.PROJECTILE_RANGE += 1f;
    HeroFactory.setHeroSkill(
        new BurningFireballSkill(
            SkillTools::cursorPositionAsPoint)); // Update the current hero skill
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
    Coordinate heroPos = EntityUtils.getHeroCoordinate();
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
  private void handleLapProgressLogic(Coordinate heroPos) {
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
  private int getCurrentCheckpoint(Coordinate heroPos) {
    for (int i = 0; i < lapCheckpoints.length; i++) {
      Coordinate[] lapCheckpoint = lapCheckpoints[i];
      for (Coordinate coordinate : lapCheckpoint) {
        if (heroPos.equals(coordinate)) {
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
  private void handleHiddenTeleporter(Coordinate heroPos) {
    for (Coordinate initTeleporterCoords : initTeleporterSpawns[0]) { // start teleporter -> in
      if (heroPos.equals(initTeleporterCoords)) {
        offsetHero(Vector2.of(27, 0));
        return;
      }
    }
    for (Coordinate lastTeleporterCoords : lastTeleporterSpawns[1]) { // end teleporter -> out
      if (heroPos.equals(lastTeleporterCoords)) {
        offsetHero(Vector2.of(-24, 0));
        this.lastCheckpoint = 0;
        this.lapProgress = 0;
        this.thirdRoom = false;
        return;
      }
    }
  }
}
