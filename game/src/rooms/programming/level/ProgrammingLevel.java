package rooms.programming.level;

import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import feature.entities.deco.Deco;
import java.util.List;
import java.util.Map;

/** Level handler for the Programming 1 escape room. */
public class ProgrammingLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "programming-1";

  /**
   * Creates the Programming 1 level.
   *
   * @param layout tile layout loaded from the level asset
   * @param designLabel visual tile design
   * @param namedPoints named points loaded from the level asset
   * @param decorations static decorations loaded from the level asset
   */
  public ProgrammingLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }
}
