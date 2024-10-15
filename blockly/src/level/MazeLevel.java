package level;

import contrib.hud.DialogUtils;
import core.level.Tile;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import level.utils.ITickable;

/**
 * This class is used for the Maze Level. The first level of the blockly dungeon. This class will
 * only set the start position of the hero and show a popup at the beginning of the level to explain
 * the target of the Maze level.
 */
public class MazeLevel extends TileLevel implements ITickable {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param heroPos The start position for the hero.
   */
  public MazeLevel(LevelElement[][] layout, DesignLabel designLabel, Point heroPos) {
    super(layout, designLabel);
    // Set Hero Position
    Tile heroTile = this.tileAt(heroPos);
    if (heroTile == null) {
      throw new RuntimeException("Invalid Hero Position: " + heroPos);
    }
    this.startTile(heroTile);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      DialogUtils.showTextPopup("Finde des Ausgang des Labyrinths!", "Ziel");
    }
  }
}
