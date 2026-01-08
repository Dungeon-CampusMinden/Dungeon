package portal.level;

import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import portal.portals.PortalColor;
import portal.portals.PortalFactory;
import portal.util.AdvancedLevel;

/** Level in the portal dungeon. */
public class PortalLevel_1 extends AdvancedLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel_1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Level 1");
  }

  @Override
  protected void onFirstTick() {
    PortalFactory.createPortal(namedPoints.get("portal1"), Direction.DOWN, PortalColor.GREEN);
    PortalFactory.createPortal(namedPoints.get("portal2"), Direction.LEFT, PortalColor.BLUE);
  }
}
