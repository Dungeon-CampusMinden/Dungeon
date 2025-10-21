package level.devlevel;

import contrib.entities.MonsterBuilder;
import contrib.entities.deco.Deco;
import contrib.hud.DialogUtils;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import entities.DevDungeonMonster;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
  private final Point[] campSpawns;
  private final Point[] mobSpawns;
  private final Point levelBossSpawn;

  private final BridgeGuardRiddleHandler riddleHandler;

  /**
   * Constructs the Bridge Guard Riddle Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public BridgeGuardRiddleLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints, List<Tuple<Deco, Point>> decorations) {
    super(
        layout,
        designLabel,
        namedPoints,
        decorations,
        "The Bridge Guard",
        "Let's try to not get lost, the entire area is brimming with orcs. Let's try to find someone who may be able to help us! The bridge should be a start.");
    this.riddleHandler = new BridgeGuardRiddleHandler(namedPoints, this);

    this.campSpawns = getPoints("Point", 13, 24);
    this.mobSpawns = getPoints("Point", 25, 53);
    this.levelBossSpawn = getPoint("Point54");
  }

  @Override
  protected void onFirstTick() {
    spawnCamps();

    Coordinate[] mobSpawns = Arrays.stream(this.mobSpawns).map(Point::toCoordinate).toArray(Coordinate[]::new);
    EntityUtils.spawnMobs(MOB_COUNT, MONSTER_TYPES, mobSpawns);
    EntityUtils.spawnBoss(
        BOSS_TYPE,
        levelBossSpawn.toCoordinate(),
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
    for (Point campSpawn : campSpawns) {
      MonsterBuilder<?>[] builders =
          Arrays.stream(MONSTER_TYPES)
              .map(DevDungeonMonster::builder)
              .toArray(MonsterBuilder[]::new);
      EntityUtils.spawnMobSpawner(campSpawn.toCoordinate(), builders, MOB_COUNT_PER_CAMP);
    }
  }
}
