package level.produs;

import contrib.hud.DialogUtils;
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

public class Chapter19Level extends BlocklyLevel {
  public static boolean showText = true;

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
    super(layout, designLabel, customPoints, "Kaptitel 1: Level 9");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.heroViewDiretion(PositionComponent.Direction.LEFT);
    if (showText) {
      DialogUtils.showTextPopup(
          "Mit diesen Spruchrollen kannst du einen mächtigen Feuerball beschwören.",
          "Kapitel 1: Ausbruch");
      showText = false;
    }
    Game.add(MiscFactory.fireballScroll(customPoints().get(0).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(1).toCenteredPoint()));
    Game.add(BlocklyMonsterFactory.hedgehog(customPoints().get(2)));
    Game.add(BlocklyMonsterFactory.hedgehog(customPoints().get(3)));
    Game.add(BlocklyMonsterFactory.hedgehog(customPoints().get(4)));
  }

  @Override
  protected void onTick() {}
}
