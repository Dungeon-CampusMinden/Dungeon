package level.produs;

import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/** In this level, simple backtracking techniques are used to find the correct path. */
public class Level017 extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level017(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 17");
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
        // Kategorien
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.heroViewDirection(Direction.DOWN);
    LevelManagementUtils.zoomDefault();
  }

  @Override
  protected void onTick() {}
}
