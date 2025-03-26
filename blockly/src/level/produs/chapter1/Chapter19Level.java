package level.produs.chapter1;

import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;

import entities.BlocklyMonsterFactory;
import entities.MiscFactory;
import level.BlocklyLevel;

public class Chapter19Level extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter19Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Chapter19");
  }

  @Override
  protected void onFirstTick() {
    Game.add(MiscFactory.fireballScroll(customPoints().get(0).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(1).toCenteredPoint()));
    Game.add(BlocklyMonsterFactory.hedgehog(customPoints().get(2),"guard 1"));
    Game.add(BlocklyMonsterFactory.hedgehog(customPoints().get(3),"guard 2"));
    Game.add(BlocklyMonsterFactory.hedgehog(customPoints().get(4),"guard 3"));

  }

  @Override
  protected void onTick() {}
}
