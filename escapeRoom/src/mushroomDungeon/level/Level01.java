package mushroomDungeon.level;

import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;

public class Level01 extends DungeonLevel {

  public Level01(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Mushroom Adventure");
  }

  @Override
  public void onFirstTick() {}

  @Override
  public void onTick() {}
}
