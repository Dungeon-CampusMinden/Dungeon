package level.devlevel;

import contrib.entities.DialogFactory;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.MonsterType;
import java.util.List;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.BridgeGuardRiddleHandler;
import level.utils.ITickable;
import starter.DevDungeon;
import utils.EntityUtils;

/** The Bridge Guard Riddle Level */
public class BridgeGuardRiddleLevel extends DevDungeonLevel implements ITickable {

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 15;
  private static final MonsterType[] MONSTER_TYPES =
      new MonsterType[] {MonsterType.ORC_WARRIOR, MonsterType.ORC_SHAMAN};
  private static final MonsterType BOSS_TYPE = MonsterType.PUMPKIN_BOI;
  private static final int MOB_COUNT_PER_CAMP = 3;

  // Spawn Points / Locations
  private final Coordinate[] campSpawns;
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;

  private final BridgeGuardRiddleHandler riddleHandler;

  public BridgeGuardRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    this.riddleHandler = new BridgeGuardRiddleHandler(customPoints, this);

    this.campSpawns = this.getCoordinates(13, 24);
    this.mobSpawns = this.getCoordinates(25, 53);
    this.levelBossSpawn = this.customPoints().get(54);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      DialogFactory.showTextPopup(
          "Let's try to not get lost, the entire area is brimming with orcs. Let's try to find someone who may be able to help us! The bridge should be a start.",
          "Level " + DevDungeon.DUNGEON_LOADER.currentLevelIndex() + ": The Bridge Guard");
      ((ExitTile) this.endTile()).close(); // close exit at start (to force defeating the boss)
      this.doorTiles().forEach(DoorTile::close);
      this.pitTiles()
          .forEach(
              pit -> {
                pit.timeToOpen(50L * Game.currentLevel().RANDOM.nextInt(1, 5));
                pit.close();
              });
      this.handleFirstTick();
    }

    this.riddleHandler.onTick(isFirstTick);
  }

  private void handleFirstTick() {
    // Spawn all entities and it's content
    this.spawnCamps();

    EntityUtils.spawnMobs(MOB_COUNT, MONSTER_TYPES, this.mobSpawns);
    EntityUtils.spawnBoss(
        BOSS_TYPE,
        this.levelBossSpawn,
        (boss) -> {
          DialogFactory.showTextPopup(
              "The next level is the final boss. You probably want to prepare for that. It's going to be tough.",
              "No Point of Return!");
        });
  }

  private void spawnCamps() {
    for (Coordinate campSpawn : this.campSpawns) {
      EntityUtils.spawnMobSpawner(campSpawn, MONSTER_TYPES, MOB_COUNT_PER_CAMP);
    }
  }
}
