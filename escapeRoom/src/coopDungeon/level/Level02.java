package coopDungeon.level;

import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;

public class Level02 extends DungeonLevel {

  public Level02(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Coop 2");
  }

  @Override
  protected void onFirstTick() {}

  @Override
  protected void onTick() {}
}
