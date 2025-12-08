package level.portal;

import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.Map;
import level.AdvancedLevel;

public class PortalLevel1 extends AdvancedLevel {
  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Demo Level");
  }
}
