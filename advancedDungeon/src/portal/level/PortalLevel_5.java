package portal.level;

import contrib.components.LeverComponent;
import core.Entity;
import core.Game;
import core.level.elements.tile.ExitTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import level.AdvancedLevel;
import portal.components.ToggleableComponent;
import portal.energyPellet.EnergyPelletLauncher;
import portal.energyPellet.EnergyPelletCatcher;
import portal.entities.AdvancedFactory;
import portal.physicsobject.Cube;
import portal.physicsobject.PressurePlates;

/**
 * Portal level five. In this level the player has to place a cube on a pressure plate to unlock the
 * exit. But in the middle of the room there is an anti-material-barrier which prevents the cube
 * from passing through it. But the anti-material-barrier can be disabled by directing the energy
 * ball in its receptor.
 */
public class PortalLevel_5 extends AdvancedLevel {
  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel_5(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Demo Level");
  }

  private ExitTile door;
  private Entity cube;
  private Entity[] walls = new Entity[9];
  private LeverComponent plate;
  private ToggleableComponent catcherToggle;

  @Override
  protected void onFirstTick() {
    door = (ExitTile) Game.randomTile(LevelElement.EXIT).orElseThrow();
    door.close();

    Entity pressurePlate = PressurePlates.cubePressurePlate(namedPoints.get("pressurePlate"), 1);
    plate = pressurePlate.fetch(LeverComponent.class).get();
    cube = Cube.portalCube(namedPoints.get("cube"));

    Entity launcher =
        EnergyPelletLauncher.energyPelletLauncher(
            namedPoints.get("pelletLauncher"), Direction.DOWN, 100000, 100000);

    Entity catcher =
        EnergyPelletCatcher.energyPelletCatcher(namedPoints.get("pelletCatcher"), Direction.LEFT);
    catcherToggle = catcher.fetch(ToggleableComponent.class).orElseThrow();

    for (int i = 0; i < 9; i++) {
      walls[i] = AdvancedFactory.antiMaterialBarrier(namedPoints.get("w" + i), true);
    }

    Game.add(pressurePlate);
    Game.add(cube);
    Game.add(launcher);
    Game.add(catcher);
    for (Entity w : walls) {
      Game.add(w);
    }
  }

  @Override
  protected void onTick() {
    if (!Game.existInLevel(cube)) {
      cube = Cube.portalCube(namedPoints.get("cube"));
      Game.add(cube);
    }

    if (catcherToggle.isActive()) {
      for (Entity w : walls) {
        Game.remove(w);
      }
    }

    if (plate.isOn()) door.open();
    else door.close();
  }
}
