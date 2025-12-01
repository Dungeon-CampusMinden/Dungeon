package level.portal;

import contrib.components.LeverComponent;
import core.Entity;
import core.Game;
import core.level.elements.tile.ExitTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import entities.AdvancedFactory;
import java.util.Map;

import entities.LightWallFactory;
import level.AdvancedLevel;

public class PortalLevel4 extends AdvancedLevel {
  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel4(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Demo Level");
  }

  @Override
  protected void onFirstTick() {
    Entity turret1 =
        AdvancedFactory.energyPelletLauncher(
            namedPoints.get("turret1"), Direction.DOWN, 10000000, 10000);
    Entity turret2 =
      AdvancedFactory.energyPelletLauncher(
        namedPoints.get("turret2"), Direction.DOWN, 10000000, 10000);
    Entity turret3 =
      AdvancedFactory.energyPelletLauncher(
        namedPoints.get("turret3"), Direction.DOWN, 10000000, 10000);

    Entity wall = LightWallFactory.createEmitter(namedPoints.get("wall"), Direction.DOWN, true);

    Game.add(turret1);
    Game.add(turret2);
    Game.add(turret3);
    Game.add(wall);
  }
}
