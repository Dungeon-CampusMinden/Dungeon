package level.produs;

import contrib.hud.DialogUtils;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import entities.MiscFactory;
import java.util.List;
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
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level003(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 3");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
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
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    if (showText) {
      DialogUtils.showTextPopup(
          "Oh nein, die Abkürzung ist versperrt. Jetzt muss ich den langen Weg nehmen. Wenn es doch nur eine Möglichkeit gäbe, die Strecke schnell zu schaffen.",
          "Kapitel 1: Ausbruch");
      showText = false;
    }
    LevelManagementUtils.cameraFocusOn(new Coordinate(13, 5));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.heroViewDirection(Direction.RIGHT);
    LevelManagementUtils.zoomDefault();
    Coordinate stone1C = customPoints().get(0);
    Coordinate stone2C = customPoints().get(1);
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
