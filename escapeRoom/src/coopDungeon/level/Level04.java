package coopDungeon.level;

import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;

public class Level04 extends DungeonLevel {

  public Level04(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Coop 4");
  }

  @Override
  protected void onFirstTick() {}

  @Override
  protected void onTick() {}
}
