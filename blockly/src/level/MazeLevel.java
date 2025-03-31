package level;

import contrib.hud.DialogUtils;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;

/**
 * This class is used for the Maze Level. The first level of the blockly dungeon. This class will
 * only set the start position of the hero and show a popup at the beginning of the level to explain
 * the target of the Maze level.
 */
public class MazeLevel extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public MazeLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Maze");
  }

  @Override
  protected void onFirstTick() {
    DialogUtils.showTextPopup("Finde des Ausgang des Labyrinths!", "Ziel");
  }

  @Override
  protected void onTick() {}
}
