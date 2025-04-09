package level.produs;

import contrib.hud.DialogUtils;
import core.Game;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.BlocklyMonsterFactory;
import java.util.List;
import java.util.function.Consumer;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter12Level extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter12Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 2");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.heroViewDiretion(PositionComponent.Direction.RIGHT);
    if (showText) {
      DialogUtils.showTextPopup(
          "Pass auf, die Monster sind angekettet und könnsen sich nicht bewegen, aber wenn du sie berührst wird es eng für dich.",
          "Kapitel 1: Ausbruch");
      showText = false;
    }

    customPoints()
        .forEach(
            new Consumer<Coordinate>() {
              @Override
              public void accept(Coordinate coordinate) {
                Game.add(BlocklyMonsterFactory.hedgehog(coordinate));
              }
            });
  }

  @Override
  protected void onTick() {}
}
