package rooms.programming.level;

import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import feature.entities.deco.Deco;
import java.util.List;
import java.util.Map;

/** Client-side level handler for the Programming 1 escape room. */
public class ProgrammingClientLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "programming-1";

  /** Creates the client representation of the Programming 1 level. */
  public ProgrammingClientLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }
}
