package level.produs;

import contrib.hud.DialogUtils;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This level features challenging backtracking that requires the use of breadcrumbs and clovers to
 * successfully navigate and solve the maze.
 */
public class Level019 extends BlocklyLevel {

  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level019(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 19");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Variable
        "get_number",
        // Bedingung
        "logic_bossView_direction",
        // Kategorien
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.zoomIn();
    LevelManagementUtils.heroViewDirection(Direction.RIGHT);
    ((DoorTile) Game.randomTile(LevelElement.DOOR).orElseThrow()).close();
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
