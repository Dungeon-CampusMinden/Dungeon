package portal.level;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import portal.lightBridge.LightBridgeFactory;
import portal.physicsobject.Cube;
import portal.physicsobject.PressurePlates;
import portal.physicsobject.Sphere;
import portal.portals.components.TractorBeamComponent;
import portal.tractorBeam.TractorBeamFactory;
import portal.util.AdvancedLevel;

/** Level Idee: Spieler, müssen zwei Arten von Platten aktivieren um den Ausgang zu öffnen. */
public class PortalLevel_7 extends AdvancedLevel {

  private LeverComponent exitPlateLever, spherePlateLever;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel_7(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Demo Level");
  }

  @Override
  protected void onFirstTick() {
    Entity tractorBeam =
        TractorBeamFactory.createTractorBeam(namedPoints.get("tractorbeam"), Direction.DOWN);
    Game.add(tractorBeam);
    closeDoor(getPoint("door3"));
    Entity exitLever =
        LeverFactory.createLever(
            namedPoints.get("catapult"),
            new ICommand() {
              @Override
              public void execute() {
                openDoor(getPoint("door3"));
              }

              @Override
              public void undo() {
                closeDoor(getPoint("door3"));
              }
            });

    Game.add(exitLever);
    Entity tractorbeamLever1 =
        LeverFactory.createLever(
            namedPoints.get("tractorbeamLever1"),
            new ICommand() {
              @Override
              public void execute() {
                TractorBeamFactory.reverseTractorBeam(
                    tractorBeam.fetch(TractorBeamComponent.class).get().getTractorBeamEntities());
              }

              @Override
              public void undo() {
                TractorBeamFactory.reverseTractorBeam(
                    tractorBeam.fetch(TractorBeamComponent.class).get().getTractorBeamEntities());
              }
            });
    Game.add(tractorbeamLever1);

    Entity tractorbeamLever2 =
        LeverFactory.createLever(
            namedPoints.get("tractorbeamLever2"),
            new ICommand() {
              @Override
              public void execute() {
                TractorBeamFactory.reverseTractorBeam(
                    tractorBeam.fetch(TractorBeamComponent.class).get().getTractorBeamEntities());
              }

              @Override
              public void undo() {
                TractorBeamFactory.reverseTractorBeam(
                    tractorBeam.fetch(TractorBeamComponent.class).get().getTractorBeamEntities());
              }
            });
    Game.add(tractorbeamLever2);

    Entity exitPlate = PressurePlates.cubePressurePlate(namedPoints.get("exitPlate"), 1);
    Game.add(exitPlate);

    exitPlateLever = exitPlate.fetch(LeverComponent.class).orElse(null);

    Entity catapultPlate = PressurePlates.spherePressurePlate(namedPoints.get("catapultPlate"), 1);
    spherePlateLever = catapultPlate.fetch(LeverComponent.class).orElse(null);
    Game.add(catapultPlate);
    Entity cube = Cube.portalCube(namedPoints.get("cube"));
    Game.add(cube);

    // Game.add(AdvancedFactory.antiMaterialBarrier(namedPoints.get("anti6"), true));

    Entity emitter =
        LightBridgeFactory.createEmitter(namedPoints.get("bridge"), Direction.LEFT, true);
    Game.add(emitter);

    Entity sphere = Sphere.portalSphere(namedPoints.get("sphere"));
    Game.add(sphere);
  }

  @Override
  protected void onTick() {

    if (exitPlateLever != null
        && exitPlateLever.isOn()
        && spherePlateLever != null
        && spherePlateLever.isOn()) {
      openDoor(namedPoints.get("door1"));
      openDoor(namedPoints.get("door2"));
    } else {
      closeDoor(namedPoints.get("door1"));
      closeDoor(namedPoints.get("door2"));
    }
  }

  private void closeDoor(Point position) {
    Tile tile = Game.tileAt(position).orElse(null);
    if (tile instanceof DoorTile) {
      ((DoorTile) tile).close();
    }
  }

  private void openDoor(Point position) {
    Tile tile = Game.tileAt(position).orElse(null);
    if (tile instanceof DoorTile) {
      ((DoorTile) tile).open();
    }
  }
}
