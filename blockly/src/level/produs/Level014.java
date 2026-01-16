package level.produs;

import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, the player faces a simple maze. The "Left Hand" maze-solving rule can be applied
 * using while loops.
 */
public class Level014 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level014(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 14");
    this.blockBlocklyElement(
        // Inventar und Charakter
        "drop_item",
        "Items",
        "wait",
        // Bedingung
        "logic_monster_direction",
        "logic_breadcrumbs_direction",
        "logic_clover_direction",
        "logic_bossView_direction",
        // Wahrheitsausdruecke
        "logic_operator",
        "usual_condition",
        // Kategorien
        "Variablen",
        "Sonstige");

    addWebPopup(new ImagePopup("popups/webpopups/level014/01_Wand.png"));
    addWebPopup(new ImagePopup("popups/webpopups/level014/02_Schleife.png"));

    addCodePopup(new ImagePopup("popups/codepopups/level014/01_Wand.png"));
    addCodePopup(new ImagePopup("popups/codepopups/level014/02_Schleife.png"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusOn(new Coordinate(5, 8));
    LevelManagementUtils.playerViewDirection(Direction.UP);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      showText = false;
      showPopups();
    }
  }

  @Override
  protected void onTick() {}
}
