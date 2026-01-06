package portal.level;

import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import level.AdvancedLevel;
import portal.portals.PortalColor;
import portal.portals.PortalFactory;

/**
 * Portal level two. In this level the player has to implement the portal gun to shoot a portal to
 * the other side of the map to avoid the death.
 */
public class PortalLevel_2 extends AdvancedLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel_2(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Level 2");
  }

  @Override
  protected void onFirstTick() {
    PortalFactory.createPortal(namedPoints.get("portal"), Direction.RIGHT, PortalColor.BLUE);
  }
}
