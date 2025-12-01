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
import entities.LightWallFactory;
import level.AdvancedLevel;

import java.util.Map;

public class PortalLevel3 extends AdvancedLevel {
  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel3(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Demo Level");
  }

  private ExitTile door;
  private Entity cube, pressurePlate, launcher1;
  private Entity[] walls = new Entity[9];
  private LeverComponent plate;

  @Override
  protected void onFirstTick() {
    door = (ExitTile) Game.randomTile(LevelElement.EXIT).get();
    door.close();

    pressurePlate = AdvancedFactory.cubePressurePlate(namedPoints.get("pressurePlate"), 1);
    plate = pressurePlate.fetch(LeverComponent.class).get();
    cube = AdvancedFactory.attachablePortalCube(namedPoints.get("cube"));

    Entity launcher =
        AdvancedFactory.energyPelletLauncher(
            namedPoints.get("pelletLauncher"), Direction.DOWN, 1000000000, 1000000000);

    for (int i = 0; i < 9; i++) {
      walls[i] = AdvancedFactory.antiMaterialBarrier(namedPoints.get("w" + i), true);
    }

    Game.add(pressurePlate);
    Game.add(cube);
    Game.add(launcher);
    for (Entity w : walls) {
      Game.add(w);
    }
  }

  @Override
  protected void onTick() {
    if (!Game.existInLevel(cube)) {
      cube = AdvancedFactory.attachablePortalCube(namedPoints.get("cube"));
      Game.add(cube);
    }

    if (plate.isOn()) door.open();
    else door.close();
  }
}
