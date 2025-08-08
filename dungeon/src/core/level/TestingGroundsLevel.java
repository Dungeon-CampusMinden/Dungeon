package core.level;

import contrib.entities.MonsterFactory;
import contrib.item.Item;
import contrib.item.concreteItem.ItemWoodenArrow;
import contrib.item.concreteItem.ItemWoodenBow;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.List;

/** This level can be used for testing new implementations of items, skills, monsters and more. */
public class TestingGroundsLevel extends DungeonLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public TestingGroundsLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
  }

  @Override
  protected void onFirstTick() {
    Item bow = new ItemWoodenBow();
    Item arrow1 = new ItemWoodenArrow(10);
    Item arrow2 = new ItemWoodenArrow(10);
    Point bowSpawnPoint = new Point(5, 5);
    Point arrowSpawnPoint1 = new Point(7, 5);
    Point arrowSpawnPoint2 = new Point(9, 5);
    bow.drop(bowSpawnPoint);
    arrow1.drop(arrowSpawnPoint1);
    arrow2.drop(arrowSpawnPoint2);

    try {
      Game.add(MonsterFactory.randomMonster());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
