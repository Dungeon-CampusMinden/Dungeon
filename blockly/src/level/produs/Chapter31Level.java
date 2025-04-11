package level.produs;

import components.AmmunitionComponent;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter31Level extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter31Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 3: Level 1");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomIn();
    LevelManagementUtils.heroViewDiretion(PositionComponent.Direction.RIGHT);
    ((DoorTile) Game.randomTile(LevelElement.DOOR).get()).close();
    Game.hero().get().fetch(AmmunitionComponent.class).orElseThrow().currentAmmunition(20);
  }

  @Override
  protected void onTick() {}
}
