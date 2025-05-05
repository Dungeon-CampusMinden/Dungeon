package level.produs;

import contrib.hud.DialogUtils;
import contrib.systems.FogSystem;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter33Level extends BlocklyLevel {

  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter33Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 3: Level 3");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.zoomIn();
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.RIGHT);
    ((DoorTile) Game.randomTile(LevelElement.DOOR).get()).close();
    if (showText) {
      DialogUtils.showTextPopup(
          "Nutz deinen Beutel mit Krumen und Kleeblättern, um deinen Weg hier raus zu finden.",
          "Kapitel 3: Rache");
      showText = false;
    }
  }

  @Override
  protected void onTick() {}
}
