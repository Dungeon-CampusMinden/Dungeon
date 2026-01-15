package level.produs;

import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import entities.MiscFactory;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, the for-loop is unlocked and should be used to efficiently move along long paths.
 * There are no monsters, but the layout encourages using loops to avoid repetitive code.
 */
public class Level003 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level003(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 3");
    this.blockBlocklyElement(
        // Richtungen
        "direction_up",
        "direction_down",
        "direction_here",
        // Schleifen
        "while_loop",
        // Kategorien
        "Inventar & Charakter",
        "Abfragen",
        "Bedingung",
        "Wahrheitsausdruecke",
        "Variablen",
        "Bedingungen",
        "Sonstige");
    addWebPopup(new ImagePopup("popups/webpopups/level003/01_steine.png"));
    addWebPopup(new ImagePopup("popups/webpopups/level003/02_loop.png"));
    addWebPopup(new ImagePopup("popups/webpopups/level003/03_loop.png"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    if (showText) {
      showPopups();
      showText = false;
    }
    LevelManagementUtils.cameraFocusOn(new Coordinate(13, 5));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.playerViewDirection(Direction.RIGHT);
    LevelManagementUtils.zoomDefault();
    Coordinate stone1C = getPoint(0).toCoordinate();
    Coordinate stone2C = getPoint(1).toCoordinate();
    Game.add(MiscFactory.stone(stone1C.toPoint()));
    Game.add(MiscFactory.stone(stone2C.toPoint()));

    Game.tileAt(new Coordinate(0, 5))
        .filter(DoorTile.class::isInstance)
        .map(DoorTile.class::cast)
        .ifPresent(DoorTile::close);
  }

  @Override
  protected void onTick() {}
}
