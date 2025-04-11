package level.produs;

import core.Game;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.BlocklyMonsterFactory;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter110Level extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter110Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 10");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.cameraFocusOn(new Coordinate(8, 6));
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.RIGHT);
    Game.add(MiscFactory.fireballScroll(customPoints().get(0).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(1).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(2).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(3).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(4).toCenteredPoint()));

    Game.add(
        BlocklyMonsterFactory.guard(customPoints().get(5), PositionComponent.Direction.DOWN, 5));
    Game.add(
        BlocklyMonsterFactory.guard(customPoints().get(6), PositionComponent.Direction.RIGHT, 5));
    Game.add(
        BlocklyMonsterFactory.guard(customPoints().get(7), PositionComponent.Direction.RIGHT, 5));
    Game.add(
        BlocklyMonsterFactory.guard(customPoints().get(8), PositionComponent.Direction.DOWN, 5));
    Game.add(
        BlocklyMonsterFactory.guard(customPoints().get(9), PositionComponent.Direction.DOWN, 5));
  }

  @Override
  protected void onTick() {}
}
