package level.devlevel.riddleHandler;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.utils.Coordinate;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.List;
import level.utils.ITickable;
import utils.EntityUtils;

public class IllusionRiddleHandler implements ITickable {

  private final TileLevel level;
  private final Coordinate[][] initTeleporterSpawns;
  private final Coordinate[][] lastTeleporterSpawns;
  private final Coordinate[][] lapCheckpoints; // [location][3 tiles wide]
  private final Coordinate[] riddleRoomBounds; // TopLeft, BottomRight
  private final Coordinate
      riddleRewardSpawn; // The spawn point of the reward for solving the riddle
  private Coordinate lastHeroPos = new Coordinate(0, 0);
  private boolean rewardGiven = false;
  private int lapCounter = 0;
  private int lapProgress = 0;
  private int lastCheckpoint =
      -2; // Initialize with -2 to indicate no checkpoints have been passed yet
  private boolean thirdRoom = false;

  public IllusionRiddleHandler(List<Coordinate> customPoints, TileLevel level) {
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
    this.riddleRoomBounds = new Coordinate[] {new Coordinate(0, 0), new Coordinate(0, 0)};
    this.riddleRewardSpawn =
        new Coordinate(0, 0); // TODO: Riddle Room with reward spawn (Token, Key)
    this.lapCheckpoints =
        new Coordinate[][] {
          {customPoints.get(148), customPoints.get(149), customPoints.get(150)}, // Right
          {customPoints.get(151), customPoints.get(152), customPoints.get(153)}, // Top
          {customPoints.get(154), customPoints.get(155), customPoints.get(156)}, // Left
          {customPoints.get(157), customPoints.get(158), customPoints.get(159)} // Bottom
        };

    this.level = level;
  }

  /**
   * This method is called when the hero moves. It updates the lap progress and counter based on the
   * hero's movement through checkpoints.
   *
   * @param heroPos The current position of the hero.
   */
  public void onHeroMove(Coordinate heroPos) {
    int currentCheckpoint = this.getCurrentCheckpoint(heroPos);
    // Check if the hero is not on a checkpoint or is on the same checkpoint as the last update
    if (currentCheckpoint == -1 || currentCheckpoint == this.lastCheckpoint) {
      return; // No update required
    }

    // Calculate the difference between the current and the last checkpoint
    int checkpointDifference = currentCheckpoint - this.lastCheckpoint;

    // Update lastCheckpoint for the next call
    this.lastCheckpoint = currentCheckpoint;

    // If this is the first checkpoint the hero reaches, just return
    if (this.lastCheckpoint == -2) {
      return;
    }

    // Moving forward through checkpoints
    if (checkpointDifference == 1 || checkpointDifference == -3) {
      this.lapProgress++;
      if (this.lapProgress > 3) {
        this.lapCounter++; // Completed a lap
        this.lapProgress = 0; // Reset progress for the next lap
      }
    }
    // Moving backward through checkpoints
    else if (checkpointDifference == -1 || checkpointDifference == 3) {
      this.lapProgress--;
      if (this.lapProgress < 0) {
        this.lapCounter--; // Completed a negative lap
        this.lapProgress = 3; // Reset progress for the next negative lap
      }
    }
    // Output the current lap and progress
    System.out.println("Lap: " + this.lapCounter + " Progress: " + this.lapProgress);
  }

  private int getCurrentCheckpoint(Coordinate heroPos) {
    for (int i = 0; i < this.lapCheckpoints.length; i++) {
      Coordinate[] lapCheckpoint = this.lapCheckpoints[i];
      for (Coordinate coordinate : lapCheckpoint) {
        if (heroPos.equals(coordinate)) {
          return i;
        }
      }
    }
    return -1;
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
    }

    Coordinate heroPos = EntityUtils.getHeroCoordinate();

    if (heroPos != null && !this.lastHeroPos.equals(heroPos)) {
      // Hero moved
      this.lastHeroPos = heroPos;
      this.onHeroMove(heroPos);

      if (this.thirdRoom && this.lapProgress == 0) {
        this.thirdRoom = false;
      }

      if (this.lapCounter == -1 && this.lapProgress == 3) {
        this.offsetHero(new Coordinate(-27, 0));
        this.lapCounter = 0;
        this.lapProgress = 0;
        this.lastCheckpoint = -2;
      } else if (!this.thirdRoom && this.lapCounter == 2 && this.lapProgress == 1) {
        this.offsetHero(new Coordinate(24, 0));
        this.thirdRoom = true;
      } else {
        this.handleHiddenTeleporter(heroPos);
      }
    }
  }

  private void handleHiddenTeleporter(Coordinate heroPos) {
    for (Coordinate initTeleporterCoords : this.initTeleporterSpawns[0]) { // start teleporter -> in
      if (heroPos.equals(initTeleporterCoords)) {
        this.offsetHero(new Coordinate(27, 0));
        return;
      }
    }
    for (Coordinate lastTeleporterCoords : this.lastTeleporterSpawns[1]) { // end teleporter -> out
      if (heroPos.equals(lastTeleporterCoords)) {
        this.offsetHero(new Coordinate(-24, 0));
        this.lastCheckpoint = 0;
        this.lapProgress = 0;
        this.thirdRoom = false;
        return;
      }
    }
  }

  private void giveReward() {

    this.rewardGiven = true;
  }

  private void handleFirstTick() {
    if (this.level.tileAt(this.riddleRewardSpawn) != null)
      this.level.tileAt(this.riddleRewardSpawn).tintColor(0x22FF22FF);
    int[] colors = {0xFF0000FF, 0x00FF00FF, 0x0000FFFF, 0xFFFF00FF};
    for (int i = 0; i < this.lapCheckpoints.length; i++) {
      Coordinate[] lapCheckpoint = this.lapCheckpoints[i];
      for (Coordinate checkpoint : lapCheckpoint) {
        if (this.level.tileAt(checkpoint) != null)
          this.level.tileAt(checkpoint).tintColor(colors[i]);
      }
    }
  }

  private void offsetHero(Coordinate offset) {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return;
    }
    PositionComponent heroPc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    Point newPoint = new Point(heroPc.position().x + offset.x, heroPc.position().y + offset.y);

    EntityUtils.teleportHeroTo(newPoint);
  }
}
