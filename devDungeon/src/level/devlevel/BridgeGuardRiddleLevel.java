package level.devlevel;

import contrib.entities.MonsterBuilder;
import contrib.hud.DialogUtils;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.DevDungeonMonster;
import java.util.Arrays;
import java.util.List;
import level.DevDungeonLevel;
import level.devlevel.riddleHandler.BridgeGuardRiddleHandler;
import utils.EntityUtils;

/** The Bridge Guard Riddle Level. */
public class BridgeGuardRiddleLevel extends DevDungeonLevel {

  // Difficulty (Mob Count, Mob Types)
  private static final int MOB_COUNT = 15;
  private static final DevDungeonMonster[] MONSTER_TYPES =
      new DevDungeonMonster[] {DevDungeonMonster.ORC_WARRIOR, DevDungeonMonster.ORC_SHAMAN};
  private static final DevDungeonMonster BOSS_TYPE = DevDungeonMonster.PUMPKIN_BOI;
  private static final int MOB_COUNT_PER_CAMP = 3;

  // Spawn Points / Locations
  private final Coordinate[] campSpawns;
  private final Coordinate[] mobSpawns;
  private final Coordinate levelBossSpawn;

  private final BridgeGuardRiddleHandler riddleHandler;

  /**
   * Constructs the Bridge Guard Riddle Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param customPoints The custom points of the level.
   */
  public BridgeGuardRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(
        layout,
        designLabel,
        customPoints,
        "The Bridge Guard",
        "Let's try to not get lost, the entire area is brimming with orcs. Let's try to find someone who may be able to help us! The bridge should be a start.");
    this.riddleHandler = new BridgeGuardRiddleHandler(customPoints, this);

    this.campSpawns = getCoordinates(13, 24);
    this.mobSpawns = getCoordinates(25, 53);
    this.levelBossSpawn = customPoints().get(54);
  }

  @Override
  protected void onFirstTick() {
    spawnCamps();

    EntityUtils.spawnMobs(MOB_COUNT, MONSTER_TYPES, mobSpawns);
    EntityUtils.spawnBoss(
        BOSS_TYPE,
        levelBossSpawn,
        (boss) -> {
          DialogUtils.showTextPopup(
              "The next level is the final boss. You probably want to prepare for that. It's going to be tough.",
              "No Point of Return!");
        });
    riddleHandler.onFirstTick();
  }

  @Override
  protected void onTick() {
    riddleHandler.onTick();
  }

  private void spawnCamps() {
    for (Coordinate campSpawn : campSpawns) {
      MonsterBuilder<?>[] builders =
          Arrays.stream(MONSTER_TYPES)
              .map(DevDungeonMonster::builder)
              .toArray(MonsterBuilder[]::new);
      EntityUtils.spawnMobSpawner(campSpawn, builders, MOB_COUNT_PER_CAMP);
    }
  }
}
