package level;

import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.MonsterType;
import entities.MonsterUtils;
import level.utils.ITickable;

/** The tutorial level */
public class DevLevel00 extends DevDungeonLevel implements ITickable {

  public DevLevel00(LevelElement[][] layout, DesignLabel designLabel) {
    super(layout, designLabel);
  }

  @Override
  public void onTick(boolean isFirstTick) {
      if (isFirstTick) {
          Coordinate coords = this.randomTile(LevelElement.FLOOR).coordinate();
          MonsterUtils.spawnMonster(MonsterType.IMP, coords);
      }
  }
}
