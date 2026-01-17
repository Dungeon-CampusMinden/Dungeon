package level.produs;

import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/** In this level, simple backtracking techniques are used to find the correct path. */
public class Level018 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level018(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 18");
    this.blockBlocklyElement(
        // Inventar und Charakter
        "drop_item",
        "Items",
        "wait",
        // Variable
        "get_number",
        "switch_case",
        "case_block",
        "default_block",
        // Bedingung
        "logic_bossView_direction",
        // Kategorien
        "Sonstige");

    addCodePopup(new ImagePopup("popups/codepopups/overview1.png"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(true);
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.playerViewDirection(Direction.RIGHT);
    ((DoorTile) Game.randomTile(LevelElement.DOOR).orElseThrow()).close();

    if (showText) {
      showPopups();
      showText = false;
    }
  }

  @Override
  protected void onTick() {}
}
