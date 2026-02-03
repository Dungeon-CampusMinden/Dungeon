package portal.level;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import portal.energyPellet.EnergyPelletCatcher;
import portal.energyPellet.EnergyPelletLauncher;
import portal.lightBridge.LightBridgeFactory;
import portal.physicsobject.Cube;
import portal.physicsobject.PressurePlates;
import portal.util.AdvancedLevel;
import portal.util.ToggleableComponent;

/**
 * Portal level just for playing without any riddles.
 */
public class PortalLevel_9 extends AdvancedLevel {
  private Entity lightBridge,
      lightBridge2,
      lever1,
      lever2,
      lever3,
      lever4,
      pressurePlate1,
      pressurePlate2;
  private DoorTile doorNorth, doorEast, doorSouth, doorWest, doorRoom1, doorRoom2;
  private ToggleableComponent catcherToggle;
  private boolean catcherAktiv = false;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel_9(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Level 9");
  }

  @Override
  protected void onFirstTick() {

    ExitTile door = (ExitTile) Game.randomTile(LevelElement.EXIT).orElseThrow();
    door.open();

    lightBridge =
        LightBridgeFactory.createEmitter(namedPoints.get("lightBridge1"), Direction.RIGHT, false);
    Game.add(lightBridge);

    lightBridge2 =
        LightBridgeFactory.createEmitter(namedPoints.get("lightBridge2"), Direction.DOWN, false);
    Game.add(lightBridge2);

    doorNorth = (DoorTile) tileAt(namedPoints.get("doorNorth")).orElse(null);
    doorEast = (DoorTile) tileAt(namedPoints.get("doorEast")).orElse(null);
    doorSouth = (DoorTile) tileAt(namedPoints.get("doorSouth")).orElse(null);
    doorWest = (DoorTile) tileAt(namedPoints.get("doorWest")).orElse(null);

    doorNorth.close();
    doorEast.close();
    doorSouth.close();
    doorWest.close();

    doorRoom1 = (DoorTile) tileAt(namedPoints.get("doorRoom1")).orElse(null);
    doorRoom2 = (DoorTile) tileAt(namedPoints.get("doorRoom2")).orElse(null);

    doorRoom1.close();
    doorRoom2.close();

    lever1 = LeverFactory.createLever(namedPoints.get("lever1"));

    Game.add(lever1);

    lever2 = LeverFactory.createLever(namedPoints.get("lever2"));

    lever3 = LeverFactory.createLever(namedPoints.get("lever3"));

    lever4 = LeverFactory.createLever(namedPoints.get("lever4"));

    Game.add(lever2);
    Game.add(lever3);
    Game.add(lever4);

    pressurePlate1 = PressurePlates.cubePressurePlate(namedPoints.get("pressurePlate1"), 1);
    Game.add(pressurePlate1);

    pressurePlate2 = PressurePlates.cubePressurePlate(namedPoints.get("pressurePlate2"), 1);
    Game.add(pressurePlate2);

    Entity cube = Cube.portalCube(namedPoints.get("cube"));
    Game.add(cube);

    Entity cube2 = Cube.portalCube(namedPoints.get("cube2"));
    Game.add(cube2);

    Entity launcher =
        EnergyPelletLauncher.energyPelletLauncher(
            namedPoints.get("pelletLauncher"), Direction.DOWN, 100000, 100000);

    Entity catcher =
        EnergyPelletCatcher.energyPelletCatcher(namedPoints.get("pelletCatcher"), Direction.RIGHT);
    catcherToggle = catcher.fetch(ToggleableComponent.class).orElseThrow();

    Game.add(catcher);
    Game.add(launcher);
  }

  @Override
  protected void onTick() {

    if (isLeverOn(lever1) && isLeverOn(lever2) && isLeverOn(lever3)) {
      doorNorth.open();
    } else {
      doorNorth.close();
    }

    if (!isLeverOn(lever3) && !isLeverOn(lever4)) {
      LightBridgeFactory.activate(lightBridge);
    } else {
      LightBridgeFactory.deactivate(lightBridge);
    }

    if (!isLeverOn(lever1) && !isLeverOn(lever3)) {
      doorRoom1.open();
    } else {
      doorRoom1.close();
    }

    if (isLeverOn(pressurePlate1)) {
      doorEast.open();
    } else {
      doorEast.close();
    }

    if (isLeverOn(pressurePlate2)) {
      doorRoom2.open();
    } else {
      doorRoom2.close();
    }

    if (isLeverOn(lever1) && isLeverOn(lever2) && isLeverOn(lever3) && isLeverOn(lever4)) {
      doorWest.open();
    } else {
      doorWest.close();
    }

    if (!isLeverOn(lever1) && !isLeverOn(lever2) && !isLeverOn(lever3) && !isLeverOn(lever4)) {
      doorSouth.open();
    } else {
      doorSouth.close();
    }

    if (catcherToggle.isActive()) {
      catcherAktiv = true;
    }

    if (catcherAktiv) {
      LightBridgeFactory.activate(lightBridge2);
    } else {
      LightBridgeFactory.deactivate(lightBridge2);
    }
  }

  private boolean isLeverOn(Entity entity) {
    return entity.fetch(LeverComponent.class).orElse(null).isOn();
  }
}
