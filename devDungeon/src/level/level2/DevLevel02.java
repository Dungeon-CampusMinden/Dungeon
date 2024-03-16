package level.level2;

import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.MonsterType;
import java.util.*;
import level.DevDungeonLevel;
import level.utils.ITickable;

/** The First Level (Torch Riddle) */
public class DevLevel02 extends DevDungeonLevel implements ITickable {

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 5;
  private static final MonsterType[] MONSTER_TYPES =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};

  // Spawn Points / Locations
  private final Coordinate[] mobSpawns;

  private final DevLevel02Riddle riddleHandler;

  public DevLevel02(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    this.riddleHandler = new DevLevel02Riddle(customPoints, this);
    this.mobSpawns = new Coordinate[0];
    // this.customPoints().subList(20, this.customPoints().size()).toArray(new Coordinate[0]);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
      this.doorTiles().forEach(DoorTile::close);
      this.pitTiles()
          .forEach(
              pit -> {
                pit.timeToOpen(50);
                pit.close();
              });
    }

    this.riddleHandler.onTick(isFirstTick);
  }

  private void handleFirstTick() {
    ((ExitTile) this.endTile()).close(); // close exit at start (to force defeating the boss)

    // Spawn all entities and it's content
    this.spawnChestsAndCauldrons();
  }

  /**
   * Spawns the chests and cauldrons in the riddle room.
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void spawnChestsAndCauldrons() {}
}
