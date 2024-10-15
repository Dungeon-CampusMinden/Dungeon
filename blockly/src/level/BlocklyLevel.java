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

  /** Layout of the level. Will be passed to the dungeon. */
  public LevelElement[][] layout;

  /** Design label of the dungeon. Will be passed to the dungeon. */
  public DesignLabel designLabel;

  /** Initial starting point of the hero in the level. */
  public Point heroPos;

  /**
   * Custom points. Can be used in a level class to define custom events when the hero is at one of
   * the custom points.
   */
  public List<Coordinate> customPoints;

  /**
   * Store the given values in this class.
   *
   * @param layout Layout of the level.
   * @param designLabel Design label of the dungeon.
   * @param heroPos Initial starting point of the hero in the level.
   * @param customPoints Custom points. Can be used in a level class to define custom events when
   *     the hero is at one of the custom points.
   */
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
