package level.produs;

import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This is the start of the game. It is designed to help players get comfortable with the Blockly
 * controls. In this level, the player can only move and turn; no monsters are present.
 */
public class Level001 extends BlocklyLevel {

  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level001(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 1");
    this.blockBlocklyElement(
        // Richtungen
        "direction_up",
        "direction_down",
        "direction_here",
        // Kategorien
        "Inventar & Charakter",
        "Abfragen",
        "Bedingung",
        "Wahrheitsausdruecke",
        "Variablen",
        "Schleife",
        "Bedingungen",
        "Sonstige");

    addWebPopup(new ImagePopup("popups/webpopups/level001/01_start_block.png"));
    addWebPopup(new ImagePopup("popups/webpopups/level001/02_skills.png"));
    addWebPopup(new ImagePopup("popups/webpopups/level001/03_commands.png"));
    addWebPopup(new ImagePopup("popups/webpopups/level001/04_start_game.png"));

    addCodePopup(new ImagePopup("popups/codepopups/level001/01_start_block.png"));
    addCodePopup(new ImagePopup("popups/codepopups/level001/02_skills.png"));
    addCodePopup(new ImagePopup("popups/codepopups/level001/03_commands.png"));
    addCodePopup(new ImagePopup("popups/codepopups/overview1.png"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.playerViewDirection(Direction.DOWN);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      showPopups();
      showText = false;
    }
  }

  @Override
  protected void onTick() {}
}
