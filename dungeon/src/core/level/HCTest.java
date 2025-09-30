package core.level;

import contrib.entities.DungeonMonster;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;
import core.utils.Point;
import core.Entity;
import core.Game;


public class HCTest extends DungeonLevel {

  public HCTest(
    LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "BlinkTestLevel");
  }

  @Override
  public void onFirstTick() {
    Point p = customPoints.get(0).toCenteredPoint();
    Entity m = DungeonMonster.randomMonster().builder().build(p);
    Game.add(m);
  }

  @Override
  public void onTick() {}
}
