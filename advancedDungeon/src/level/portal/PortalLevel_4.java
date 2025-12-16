package level.portal;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import core.Entity;
import core.Game;
import core.level.elements.tile.ExitTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import entities.AdvancedFactory;
import entities.LightBridgeFactory;
import java.util.Map;
import level.AdvancedLevel;

/**
 * Portal level four. In this level there are three platforms. The player has to reach platform 2 to
 * toggle a lever which spawns a cube. This cube has to be placed on a pressure plate on platform 3
 * to unlock the exit.
 */
public class PortalLevel_4 extends AdvancedLevel {

  private Entity cube, pressurePlate;
  private LeverComponent plate, lever;

  private ExitTile door;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel_4(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Demo Level");
  }

  @Override
  protected void onFirstTick() {
    pressurePlate = AdvancedFactory.cubePressurePlate(namedPoints.get("pressurePlate"), 1);
    plate = pressurePlate.fetch(LeverComponent.class).orElseThrow();

    Entity leverEntity = LeverFactory.createLever(namedPoints.get("lever"));
    lever = leverEntity.fetch(LeverComponent.class).orElseThrow();

    Entity lightBridge =
        LightBridgeFactory.createEmitter(namedPoints.get("bridge"), Direction.LEFT, true);

    cube = AdvancedFactory.attachablePortalCube(namedPoints.get("cube"));

    door = (ExitTile) Game.randomTile(LevelElement.EXIT).orElseThrow();
    door.close();

    Game.add(leverEntity);
    Game.add(pressurePlate);
    Game.add(lightBridge);
  }

  @Override
  protected void onTick() {
    if (lever.isOn()) {
      if (!Game.existInLevel(cube)) Game.add(cube);
    }

    if (plate.isOn()) door.open();
    else door.close();
  }
}
