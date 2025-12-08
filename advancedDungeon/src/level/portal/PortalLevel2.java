package level.portal;

import contrib.components.LeverComponent;
import core.Entity;
import core.Game;
import core.level.elements.tile.ExitTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import entities.AdvancedFactory;
import java.util.Map;
import level.AdvancedLevel;

public class PortalLevel2 extends AdvancedLevel {

  private Entity cube, pressurePlate;
  private LeverComponent plate;

  private ExitTile door;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel2(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Demo Level");
  }

  @Override
  protected void onFirstTick() {
    pressurePlate = AdvancedFactory.cubePressurePlate(namedPoints.get("pressurePlate"), 1);
    plate = pressurePlate.fetch(LeverComponent.class).get();
    cube = AdvancedFactory.attachablePortalCube(namedPoints.get("cube"));

    door = (ExitTile) Game.randomTile(LevelElement.EXIT).get();
    door.close();

    Game.add(cube);
    Game.add(pressurePlate);
  }

  @Override
  protected void onTick() {
    if (plate.isOn()) door.open();
    else door.close();
  }
}
