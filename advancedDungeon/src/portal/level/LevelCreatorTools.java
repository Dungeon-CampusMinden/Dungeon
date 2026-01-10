package portal.level;

import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import contrib.utils.DynamicCompiler;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import portal.laserGrid.LaserGridSwitch;
import portal.lightBridge.BridgeSwitch;
import portal.lightWall.LightWallSwitch;
import portal.physicsobject.PortalCube;
import portal.physicsobject.PortalSphere;
import portal.physicsobject.PressurePlates;
import portal.tractorBeam.TractorBeamLever;
import starter.PortalStarter;

/**
 * Utility class for creating interactive level elements used in the Portal dungeon.
 *
 * <p>This class provides factory-like helper methods for wiring common puzzle mechanics such as
 * doors, levers, pressure plates, laser grids, bridges, light walls, and tractor beams. Most
 * interactions are implemented via {@link ICommand} instances that encapsulate execute/undo
 * behavior.
 *
 * <p>Several mechanics rely on dynamically compiled user code via {@link DynamicCompiler}. This
 * allows students or level designers to customize behavior by providing their own implementations
 * without modifying the core engine.
 *
 * <p>The class is intentionally stateless and exposes only static methods.
 */
public class LevelCreatorTools {
  private static final SimpleIPath CUBE_PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyCube.java");
  private static final String CUBE_CLASSNAME = "portal.riddles.MyCube";
  private static final SimpleIPath SPHERE_PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MySphere.java");
  private static final String SPHERE_CLASSNAME = "portal.riddles.MySphere";
  private static final SimpleIPath LASER_PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyLaserGridSwitch.java");
  private static final String LASER_CLASSNAME = "portal.riddles.MyLaserGridSwitch";
  private static final SimpleIPath BRIDGESWITCH_PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyBridgeSwitch.java");
  private static final String BRIDGESWITCH_CLASSNAME = "portal.riddles.MyBridgeSwitch";
  private static final String WALLSWITCH_CLASSNAME = "portal.riddles.MyLightWallSwitch";
  private static final SimpleIPath WALLSWITCH_PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyLightWallSwitch.java");
  private static final SimpleIPath BEAMSWITCH_PATH =
      new SimpleIPath("advancedDungeon/src/portal/riddles/MyTractorBeamLever.java");
  private static final String BEAMSWITCH_CLASSNAME = "portal.riddles.MyTractorBeamLever";
  private static final String UNDOTRACTORLEVER =
      "Der Traktorstrahl konnte nicht zurückgesetzt werden. Überprüfe, ob dein Traktorstrahl-Lever korrekt implementiert ist und das übergebene Objekt gültig ist.";

  private static final String EXECUTETRACORLEVER =
      "Der Traktorstrahl konnte nicht umgedreht werden. Stelle sicher, dass deine Lever-Logik korrekt geladen wird und der Traktorstrahl existiert.";

  private static final String UNDOWALLEVER =
      "Die Lichtwand konnte nicht deaktiviert werden. Prüfe, ob dein LightWallSwitch korrekt implementiert ist und ein gültiger Emitter übergeben wurde.";

  private static final String EXECUTEWALLLEVER =
      "Die Lichtwand konnte nicht aktiviert werden. Überprüfe deine Implementierung des LightWallSwitch und ob der Emitter korrekt referenziert wird.";

  private static final String UNDOBRDIGELEVER =
      "Die Lichtbrücke konnte nicht deaktiviert werden. Stelle sicher, dass dein BridgeSwitch korrekt implementiert ist und die Brücke existiert.";

  private static final String EXECUTEBRIDGELEVER =
      "Die Lichtbrücke konnte nicht aktiviert werden. Überprüfe, ob dein BridgeSwitch korrekt geladen wird und die Brücke korrekt übergeben wurde.";

  private static final String UNDOLASERGRIDCUBEPLATE =
      "Das Lasergitter konnte nicht wieder aktiviert werden. Prüfe deine Implementierung des LaserGridSwitch und ob die übergebenen Lasergitter gültig sind.";

  private static final String EXECUTEUNDOLASERGRIDCUBEPLATE =
      "Das Lasergitter konnte nicht deaktiviert werden. Stelle sicher, dass dein LaserGridSwitch korrekt implementiert ist und alle Lasergitter existieren.";

  private static final String CUBESPAWNER =
      "Der Würfel konnte nicht erzeugt werden. Überprüfe deine Cube-Klasse, den Rückgabewert der spawn-Methode und die angegebene Spawn-Position.";

  private static final String SPHERESPAWNER =
      "Die Kugel konnte nicht erzeugt werden. Überprüfe deine Sphere-Klasse, den Rückgabewert der spawn-Methode und die angegebene Spawn-Position.";

  /**
   * Creates a lever that opens and closes a door.
   *
   * <p>The door is initially closed. Pulling the lever opens the door, and undoing the action
   * closes it again.
   *
   * @param leverP the position of the lever entity
   * @param doorP the position of the door tile to be controlled
   * @return an {@link Entity} representing the lever
   * @throws java.util.NoSuchElementException if no tile exists at {@code doorP}
   */
  public static Entity doorLever(Point leverP, Point doorP) {
    DoorTile door = (DoorTile) Game.tileAt(doorP).orElseThrow();
    door.close();

    return LeverFactory.createLever(
        leverP,
        new ICommand() {
          @Override
          public void execute() {
            door.open();
          }

          @Override
          public void undo() {
            door.close();
          }
        });
  }

  /**
   * Creates a pressure plate that reacts to cubes and controls a door.
   *
   * <p>The door is initially closed. When an object with sufficient mass is placed on the plate,
   * the door opens. Removing the object closes the door.
   *
   * @param plateP the position of the pressure plate
   * @param doorP the position of the door tile to be controlled
   * @param mass the minimum mass required to trigger the plate
   * @return an {@link Entity} representing the pressure plate
   * @throws java.util.NoSuchElementException if no tile exists at {@code doorP}
   */
  public static Entity doorPressurePlate(Point plateP, Point doorP, float mass) {
    DoorTile door = (DoorTile) Game.tileAt(doorP).orElseThrow();
    door.close();
    return PressurePlates.cubePressurePlate(
        plateP,
        mass,
        new ICommand() {
          @Override
          public void execute() {
            door.open();
          }

          @Override
          public void undo() {
            door.close();
          }
        });
  }

  /**
   * Creates a pressure plate that reacts to spheres and controls a door.
   *
   * <p>The door is initially closed. When a sphere with sufficient mass is placed on the plate, the
   * door opens. Removing the sphere closes the door.
   *
   * @param plateP the position of the pressure plate
   * @param doorP the position of the door tile to be controlled
   * @param mass the minimum mass required to trigger the plate
   * @return an {@link Entity} representing the pressure plate
   * @throws java.util.NoSuchElementException if no tile exists at {@code doorP}
   */
  public static Entity doorPressurePlateSphere(Point plateP, Point doorP, float mass) {
    DoorTile door = (DoorTile) Game.tileAt(doorP).orElseThrow();
    door.close();
    return PressurePlates.spherePressurePlate(
        plateP,
        mass,
        new ICommand() {
          @Override
          public void execute() {
            door.open();
          }

          @Override
          public void undo() {
            door.close();
          }
        });
  }

  /**
   * Creates a lever that spawns a cube at a given position.
   *
   * <p>The cube implementation is loaded dynamically using {@link DynamicCompiler}. If the cube
   * spawns at the expected position, it is added to the game world. Undoing the action removes the
   * cube again.
   *
   * <p>If dynamic compilation or spawning fails, an error dialog is shown.
   *
   * @param position the position of the lever
   * @param cubeSpawnPosition the position where the cube should spawn
   * @return an {@link Entity} representing the lever
   */
  public static Entity cubeSpawner(Point position, Point cubeSpawnPosition) {
    return LeverFactory.createLever(
        position,
        new ICommand() {

          private Entity cube;

          @Override
          public void execute() {
            Object o;
            try {
              o = DynamicCompiler.loadUserInstance(CUBE_PATH, CUBE_CLASSNAME);
              cube = ((PortalCube) o).spawn(cubeSpawnPosition);
              PositionComponent pc = cube.fetch(PositionComponent.class).orElseThrow();
              if (pc.position().equals(cubeSpawnPosition)) {
                Game.add(cube);
              } else throw new Exception();

            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup(CUBESPAWNER, "Code Error");
            }
          }

          @Override
          public void undo() {
            if (cube != null) Game.remove(cube);
          }
        });
  }

  /**
   * Creates a lever that spawns a sphere at a given position.
   *
   * <p>The sphere implementation is loaded dynamically using {@link DynamicCompiler}. If the sphere
   * spawns at the expected position, it is added to the game world. Undoing the action removes the
   * sphere again.
   *
   * <p>If dynamic compilation or spawning fails, an error dialog is shown.
   *
   * @param position the position of the lever
   * @param cubeSpawnPosition the position where the sphere should spawn
   * @return an {@link Entity} representing the lever
   */
  public static Entity sphereSpawner(Point position, Point cubeSpawnPosition) {
    return LeverFactory.createLever(
        position,
        new ICommand() {

          private Entity sphere;

          @Override
          public void execute() {
            Object o;
            try {
              o = DynamicCompiler.loadUserInstance(SPHERE_PATH, SPHERE_CLASSNAME);
              sphere = ((PortalSphere) o).spawn(cubeSpawnPosition);
              PositionComponent pc = sphere.fetch(PositionComponent.class).orElseThrow();
              if (pc.position().equals(cubeSpawnPosition)) {
                Game.add(sphere);
              } else throw new Exception();

            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup(SPHERESPAWNER, "Code Error");
            }
          }

          @Override
          public void undo() {
            if (sphere != null) Game.remove(sphere);
          }
        });
  }

  /**
   * Creates a cube-activated pressure plate that controls one or more laser grids.
   *
   * <p>When activated, the laser grids are deactivated. Undoing the action reactivates them.
   *
   * <p>The laser grid switch logic is loaded dynamically using {@link DynamicCompiler}.
   *
   * @param platePosition the position of the pressure plate
   * @param mass the minimum mass required to trigger the plate
   * @param lasergrid one or more laser grid entities to be controlled
   * @return an {@link Entity} representing the pressure plate
   */
  public static Entity laserCubePlate(Point platePosition, float mass, Entity... lasergrid) {
    return PressurePlates.cubePressurePlate(
        platePosition,
        mass,
        new ICommand() {
          @Override
          public void execute() {

            Object o;
            try {
              o = DynamicCompiler.loadUserInstance(LASER_PATH, LASER_CLASSNAME);
              LaserGridSwitch laser = ((LaserGridSwitch) o);
              laser.deactivate(lasergrid);
            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup(EXECUTEUNDOLASERGRIDCUBEPLATE, "Code Error");
            }
          }

          @Override
          public void undo() {
            Object o;
            try {
              o = DynamicCompiler.loadUserInstance(LASER_PATH, LASER_CLASSNAME);
              LaserGridSwitch laser = ((LaserGridSwitch) o);
              laser.activate(lasergrid);
            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup(UNDOLASERGRIDCUBEPLATE, "Code Error");
            }
          }
        });
  }

  /**
   * Creates a lever that activates and deactivates a light bridge.
   *
   * <p>Pulling the lever activates the bridge. Undoing the action deactivates it.
   *
   * <p>The bridge control logic is loaded dynamically using {@link DynamicCompiler}.
   *
   * @param bridge the bridge entity to be controlled
   * @param leverPosition the position of the lever
   * @return an {@link Entity} representing the lever
   */
  public static Entity bridgeLever(Entity bridge, Point leverPosition) {
    return LeverFactory.createLever(
        leverPosition,
        new ICommand() {
          @Override
          public void execute() {

            Object o;
            try {
              o = DynamicCompiler.loadUserInstance(BRIDGESWITCH_PATH, BRIDGESWITCH_CLASSNAME);
              BridgeSwitch s = ((BridgeSwitch) o);
              s.activate(bridge);
            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup(EXECUTEBRIDGELEVER, "Code Error");
            }
          }

          @Override
          public void undo() {

            Object o;
            try {
              o = DynamicCompiler.loadUserInstance(BRIDGESWITCH_PATH, BRIDGESWITCH_CLASSNAME);
              BridgeSwitch s = ((BridgeSwitch) o);
              s.deactivate(bridge);
            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup(UNDOBRDIGELEVER, "Code Error");
            }
          }
        });
  }

  /**
   * Creates a lever that activates and deactivates a light wall.
   *
   * <p>The lever toggles the state of the light wall emitter. Undoing the action restores the
   * previous state.
   *
   * <p>The wall switch logic is loaded dynamically using {@link DynamicCompiler}.
   *
   * @param emitter the light wall emitter entity
   * @param aSwitch the position of the lever
   * @return an {@link Entity} representing the lever
   */
  public static Entity wallLever(Entity emitter, Point aSwitch) {
    return LeverFactory.createLever(
        aSwitch,
        new ICommand() {
          @Override
          public void execute() {

            Object o;
            try {
              o = DynamicCompiler.loadUserInstance(WALLSWITCH_PATH, WALLSWITCH_CLASSNAME);
              LightWallSwitch s = ((LightWallSwitch) o);
              s.activate(emitter);
            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup(EXECUTEWALLLEVER, "Code Error");
            }
          }

          @Override
          public void undo() {

            Object o;
            try {
              o = DynamicCompiler.loadUserInstance(WALLSWITCH_PATH, WALLSWITCH_CLASSNAME);
              LightWallSwitch s = ((LightWallSwitch) o);
              s.deactivate(emitter);
            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup(UNDOWALLEVER, "Code Error");
            }
          }
        });
  }

  /**
   * Creates a lever that reverses the direction of a tractor beam.
   *
   * <p>Both execution and undo reverse the tractor beam, effectively toggling its direction each
   * time the lever state changes.
   *
   * <p>The tractor beam logic is loaded dynamically using {@link DynamicCompiler}.
   *
   * @param tractorbeam the tractor beam entity to be controlled
   * @param aSwitch the position of the lever
   * @return an {@link Entity} representing the lever
   */
  public static Entity tractorLever(Entity tractorbeam, Point aSwitch) {
    return LeverFactory.createLever(
        aSwitch,
        new ICommand() {
          @Override
          public void execute() {

            Object o;
            try {
              o = DynamicCompiler.loadUserInstance(BEAMSWITCH_PATH, BEAMSWITCH_CLASSNAME);
              ((TractorBeamLever) o).reverse(tractorbeam);
            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup(EXECUTETRACORLEVER, "Code Error");
            }
          }

          @Override
          public void undo() {

            Object o;
            try {
              o = DynamicCompiler.loadUserInstance(BEAMSWITCH_PATH, BEAMSWITCH_CLASSNAME);
              ((TractorBeamLever) o).reverse(tractorbeam);
            } catch (Exception e) {
              if (PortalStarter.DEBUG_MODE) e.printStackTrace();
              DialogUtils.showTextPopup(UNDOTRACTORLEVER, "Code Error");
            }
          }
        });
  }
}
