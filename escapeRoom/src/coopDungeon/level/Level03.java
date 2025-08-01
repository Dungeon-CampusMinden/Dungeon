package coopDungeon.level;

import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;

public class Level03 extends DungeonLevel {

  public Level03(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Coop 3");
  }

  @Override
  protected void onFirstTick() {}

  @Override
  protected void onTick() {}
}
