package level;

import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;

/**
 * This class is used to store the values from a parsed level file. It contains the layout (the
 * tiles), the design label, the hero start position and the custom points. This class is used in
 * the LevelParser.
 */
public abstract class BlocklyLevel extends DevDungeonLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   * @param name The name of the level.
   */
  public BlocklyLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      List<Coordinate> customPoints,
      String name) {
    super(layout, designLabel, customPoints, name, "");
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      onFirstTick();
    } else {
      onTick();
    }
  }
}
