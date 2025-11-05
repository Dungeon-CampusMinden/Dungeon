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
 * This level focuses on pattern recognition in the paths. The route to the goal consists of
 * repeating segments.
 */
public class Level015 extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level015(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 15");
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

    addWebPopup(new ImagePopup("popups/level015/Variable.png"));
    addWebPopup(new ImagePopup("popups/level015/Variable2.png"));
    addWebPopup(new ImagePopup("popups/level015/Variable3.png"));
    addWebPopup(new ImagePopup("popups/level015/Variable4.png"));
    addWebPopup(new ImagePopup("popups/level015/Variable5.png"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    showPopups();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusOn(new Coordinate(10, 8));
    LevelManagementUtils.playerViewDirection(Direction.DOWN);
    LevelManagementUtils.zoomDefault();
  }

  @Override
  protected void onTick() {}
}
