package portal.level;

import core.Entity;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Map;
import portal.laserGrid.LaserGrid;
import portal.portals.PortalColor;
import portal.portals.PortalFactory;
import portal.util.AdvancedLevel;

public class ObjectsPortalLevel_1 extends AdvancedLevel {

  private static final String NAME = "Portal Level";

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public ObjectsPortalLevel_1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, NAME);
  }

  @Override
  protected void onFirstTick() {
    PortalFactory.createPortal(namedPoints.get("portal1"), Direction.DOWN, PortalColor.BLUE);
    PortalFactory.createPortal(namedPoints.get("portal2"), Direction.LEFT, PortalColor.GREEN);
    Game.add(LevelCreatorTools.cubeSpawner(getPoint("spawner"), getPoint("cube")));
    Entity laser = LaserGrid.laserGrid(getPoint("laser"), true);
    Entity laser2 = LaserGrid.laserGrid(getPoint("laser").translate(Vector2.of(0, -1)), true);
    Entity laser3 = LaserGrid.laserGrid(getPoint("laser").translate(Vector2.of(0, -2)), true);
    Entity laser4 = LaserGrid.laserGrid(getPoint("laser").translate(Vector2.of(0, -3)), true);
    Entity laser5 = LaserGrid.laserGrid(getPoint("laser").translate(Vector2.of(0, -4)), true);
    Game.add(laser);
    Game.add(laser2);
    Game.add(laser3);
    Game.add(laser4);
    Game.add(laser5);
    Game.add(
        LevelCreatorTools.laserCubePlate(
            getPoint("plate"), 2, laser, laser2, laser3, laser4, laser5));
  }

  @Override
  protected void onTick() {}
}
