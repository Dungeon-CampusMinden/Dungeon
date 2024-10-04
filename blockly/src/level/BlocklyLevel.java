package level;

import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.List;

/**
 * This class is used to store the values from a parsed level file. It contains the layout (the
 * tiles), the design label, the hero start position and the custom points. This class is used in
 * the LevelParser.
 */
public class BlocklyLevel {

  public LevelElement[][] layout;
  public DesignLabel designLabel;
  public Point heroPos;
  public List<Coordinate> customPoints;

  public BlocklyLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Point heroPos,
      List<Coordinate> customPoints) {
    this.layout = layout;
    this.designLabel = designLabel;
    this.heroPos = heroPos;
    this.customPoints = customPoints;
  }
}
