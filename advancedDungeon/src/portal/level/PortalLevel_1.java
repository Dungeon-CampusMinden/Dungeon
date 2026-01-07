package portal.level;

import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Map;
import portal.physicsobject.SpawnLever;
import portal.portals.PortalColor;
import portal.portals.PortalFactory;
import portal.util.AdvancedLevel;

/**
 * Portal level one. In this level the player has to implement basic controls to reach the exit on
 * the other side of the level.
 */
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
    PortalFactory.createPortal(namedPoints.get("portal2"), Direction.DOWN, PortalColor.BLUE);

    Game.add(SpawnLever.spawnLever(namedPoints.get("portal1").translate(Vector2.of(-1, -1))));
  }
}
