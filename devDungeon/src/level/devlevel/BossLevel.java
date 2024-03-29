package level.devlevel;

import core.Entity;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.MonsterType;
import java.util.List;
import level.DevDungeonLevel;
import level.utils.ITickable;
import utils.EntityUtils;

public class BossLevel extends DevDungeonLevel implements ITickable {

  // Difficulty
  public static final int BOSS_HP = 100;
  private static final MonsterType BOSS_TYPE = MonsterType.FINAL_BOSS;

  // Spawn Points / Locations
  private final Coordinate levelBossSpawn;
  private Entity boss;

  public BossLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);

    this.levelBossSpawn = this.customPoints().getFirst();
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
  }

  private void handleFirstTick() {
    this.boss =
        EntityUtils.spawnBoss(
            BOSS_TYPE,
            this.levelBossSpawn,
            (bossEntity) -> {
              ((ExitTile) this.endTile()).open();
            });
  }
}
