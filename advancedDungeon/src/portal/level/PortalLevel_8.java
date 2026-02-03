package portal.level;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.systems.EventScheduler;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import portal.lightBridge.LightBridgeFactory;
import portal.physicsobject.PressurePlates;
import portal.tractorBeam.TractorBeamComponent;
import portal.tractorBeam.TractorBeamFactory;
import portal.util.AdvancedLevel;
import portal.physicsobject.Cube;

import java.util.Map;

public class PortalLevel_8 extends AdvancedLevel {
  private LeverComponent plate, plate2, plate3, plate4, plate5;
  private ExitTile door;
  private Entity pressurePlate, lever, lightBridge, lightBridge2, pressurePlate2, pressurePlate3, pressurePlate4, pressurePlate5, tractorBeam, lightBridge3, lightBridge4  ;
  private EventScheduler.ScheduledAction myTask, tractorBeamTask1, tractorBeamTask2;
  private boolean isLightBridgeOn = false;
  private DoorTile door1, door2;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel_8(
    LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Level 8");
  }

  @Override
  protected void onFirstTick() {

    lightBridge = LightBridgeFactory.createEmitter(namedPoints.get("lightBridge"), Direction.UP, false);
    lightBridge2 = LightBridgeFactory.createEmitter(namedPoints.get("lightBridge2"), Direction.UP, false);
    lightBridge3 = LightBridgeFactory.createEmitter(namedPoints.get("lightBridge3"), Direction.UP, false);
    lightBridge4 = LightBridgeFactory.createEmitter(namedPoints.get("lightBridge4"), Direction.RIGHT, false);
//
    Game.add(lightBridge);
    Game.add(lightBridge2);
    Game.add(lightBridge3);
    Game.add(lightBridge4);
//
//
    pressurePlate = PressurePlates.cubePressurePlate(namedPoints.get("pressurePlate"), 1);
    Game.add(pressurePlate);
    plate = pressurePlate.fetch(LeverComponent.class).orElse(null);
//
//
    Entity cube = Cube.portalCube(namedPoints.get("cube1"));
    Game.add(cube);

    Entity cube2 = Cube.portalCube(namedPoints.get("cube2"));
    Game.add(cube2);

    tractorBeam = TractorBeamFactory.createTractorBeam(namedPoints.get("traktorBeam"), Direction.LEFT);

    tractorBeam.fetch(TractorBeamComponent.class).get().deactivate();

    pressurePlate2 = PressurePlates.cubePressurePlate(namedPoints.get("pressurePlate2"), 1);
    Game.add(pressurePlate2);
    plate2 = pressurePlate2.fetch(LeverComponent.class).orElse(null);

    pressurePlate3 = PressurePlates.cubePressurePlate(namedPoints.get("pressurePlate3"), 1);
    Game.add(pressurePlate3);
    plate3 = pressurePlate3.fetch(LeverComponent.class).orElse(null);

    pressurePlate4 = PressurePlates.cubePressurePlate(namedPoints.get("pressurePlate4"), 1);
    Game.add(pressurePlate4);
    plate4 = pressurePlate4.fetch(LeverComponent.class).orElse(null);

    pressurePlate5 = PressurePlates.cubePressurePlate(namedPoints.get("pressurePlate5"), 1);
    Game.add(pressurePlate5);
    plate5 = pressurePlate5.fetch(LeverComponent.class).orElse(null);


    lever =
      LeverFactory.createLever(
        namedPoints.get("lever"));

    Game.add(lever);

    door1 = (DoorTile) tileAt(namedPoints.get("door1")).orElse(null);
    door1.close();

    door2 = (DoorTile) tileAt(namedPoints.get("door2")).orElse(null);
    door2.close();

    ExitTile door = (ExitTile) Game.randomTile(LevelElement.EXIT).orElseThrow();
    door.open();

  }

  @Override
  protected void onTick() {
    checkLightBridge();
    checkTractorBeams();

    if (isLeverOn(lever)) {
      door2.open();
    } else {
      door2.close();
    }


    if (plate4.isOn() && plate5.isOn()) {
      LightBridgeFactory.activate(lightBridge4);
      door1.open();
      door2.close();
    } else {
      LightBridgeFactory.deactivate(lightBridge4);
      door1.close();
    }

  }

  private boolean isLeverOn(Entity entity) {
    return entity.fetch(LeverComponent.class).orElse(null).isOn();
  }

  private TractorBeamComponent getTractorBeamComponent(Entity beam) {
    return beam.fetch(TractorBeamComponent.class).get();
  }

  private void checkTractorBeams() {
    if (plate2.isOn()) {
      getTractorBeamComponent(tractorBeam).activate();

      if (tractorBeamTask1 != null) {
        EventScheduler.cancelAction(tractorBeamTask1);
        tractorBeamTask1 = null;
      }

      if (getTractorBeamComponent(tractorBeam).isReversed()) {
        TractorBeamFactory.reverseTractorBeam(tractorBeam.fetch(TractorBeamComponent.class).get().getTractorBeamEntities());
      }

    } else {
      if (tractorBeamTask1 == null && getTractorBeamComponent(tractorBeam).isActive() && !getTractorBeamComponent(tractorBeam).isReversed()) {
        tractorBeamTask1 = EventScheduler.scheduleAction(
          () -> {
            getTractorBeamComponent(tractorBeam).deactivate();
          },
          3000);
      }
    }

    if (plate3.isOn()) {
      tractorBeam.fetch(TractorBeamComponent.class).get().activate();

      if (tractorBeamTask2 != null) {
        EventScheduler.cancelAction(tractorBeamTask2);
        tractorBeamTask2 = null;
      }

      if (!getTractorBeamComponent(tractorBeam).isReversed()) {
        TractorBeamFactory.reverseTractorBeam(tractorBeam.fetch(TractorBeamComponent.class).get().getTractorBeamEntities());
      }

    } else {
      if (tractorBeamTask2 == null && getTractorBeamComponent(tractorBeam).isActive() && getTractorBeamComponent(tractorBeam).isReversed()) {
        tractorBeamTask2 = EventScheduler.scheduleAction(
          () -> {
            getTractorBeamComponent(tractorBeam).deactivate();
          },
          3000
        );
      }
    }

  }

  private void checkLightBridge() {
    if (plate.isOn() ) {

      if (myTask != null) {
        EventScheduler.cancelAction(myTask);
        myTask = null;
      }

      LightBridgeFactory.activate(lightBridge);
      LightBridgeFactory.activate(lightBridge2);
      LightBridgeFactory.activate(lightBridge3);
      isLightBridgeOn = true;

    } else {
      if (myTask == null && isLightBridgeOn) {
        myTask = EventScheduler.scheduleAction(
          () -> {
            LightBridgeFactory.deactivate(lightBridge);
            LightBridgeFactory.deactivate(lightBridge2);
            LightBridgeFactory.deactivate(lightBridge3);
            isLightBridgeOn = false;
            myTask = null;
          },
          800);
      }
    }
  }

}




