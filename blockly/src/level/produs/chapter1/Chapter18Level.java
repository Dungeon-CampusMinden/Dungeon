package level.produs.chapter1;

import core.Game;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.BlocklyMonsterFactory;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;
import level.Utils;

public class Chapter18Level extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter18Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Chapter18");
  }

  @Override
  protected void onFirstTick() {
    Utils.cameraFocusOn(new Coordinate(8, 6));
    Game.add(MiscFactory.fireballScroll(customPoints().get(0).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(1).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(2).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(3).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(4).toCenteredPoint()));

    Game.add(
        BlocklyMonsterFactory.guard(
            customPoints().get(5), "guard 1", PositionComponent.Direction.DOWN));
    Game.add(
        BlocklyMonsterFactory.guard(
            customPoints().get(6), "guard 2", PositionComponent.Direction.RIGHT));
    Game.add(
        BlocklyMonsterFactory.guard(
            customPoints().get(7), "guard 3", PositionComponent.Direction.RIGHT));
    Game.add(
        BlocklyMonsterFactory.guard(
            customPoints().get(8), "guard 4", PositionComponent.Direction.DOWN));
    Game.add(
        BlocklyMonsterFactory.guard(
            customPoints().get(9), "guard 5", PositionComponent.Direction.DOWN));
  }

  @Override
  protected void onTick() {}
}
