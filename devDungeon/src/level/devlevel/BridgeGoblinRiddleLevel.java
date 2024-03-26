package level.devlevel;

import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.MonsterType;
import java.util.List;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.BridgeGoblinRiddleHandler;
import level.utils.ITickable;

/** The Bridge Goblin Riddle Level */
public class BridgeGoblinRiddleLevel extends DevDungeonLevel implements ITickable {

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 0;
  private static final MonsterType[] MONSTER_TYPES =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};
  private static final MonsterType BOSS_TYPE = MonsterType.CHORT;

  // Spawn Points / Locations
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;

  private final BridgeGoblinRiddleHandler riddleHandler;

  public BridgeGoblinRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    this.riddleHandler = new BridgeGoblinRiddleHandler(customPoints, this);
    this.mobSpawns = new Coordinate[] {new Coordinate(0, 0), new Coordinate(0, 0)};
    this.levelBossSpawn = new Coordinate(0, 0);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      ((ExitTile) this.endTile()).close(); // close exit at start (to force defeating the boss)
      this.doorTiles().forEach(DoorTile::close);
      this.pitTiles()
          .forEach(
              pit -> {
                pit.timeToOpen(50);
                pit.close();
              });
      this.handleFirstTick();
    }

    this.riddleHandler.onTick(isFirstTick);
  }

  private void handleFirstTick() {

    this.prepareBridge();

    // Spawn all entities and it's content
    this.spawnChestsAndCauldrons();

    // EntityUtils.spawnMobs(MOB_COUNT, MONSTER_TYPES, this.mobSpawns, BOSS_TYPE,
    // this.levelBossSpawn);
  }

  private void prepareBridge() {
    // EntityUtils.spawnMonster(MonsterType.BRIDGE_GOBLIN, this.bridgeMobSpawn);

  }

  /**
   * Spawns the chests and cauldrons in the riddle room.
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void spawnChestsAndCauldrons() {}
}
