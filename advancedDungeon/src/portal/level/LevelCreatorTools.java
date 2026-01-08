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
import portal.physicsobject.PortalCube;
import portal.physicsobject.PortalSphere;
import portal.physicsobject.PressurePlates;
import starter.PortalStarter;

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

  public static Entity doorPressurePlate(Point plateP, Point doorP, float mass) {
    DoorTile door = (DoorTile) Game.tileAt(doorP).orElseThrow();
    door.close();
    return PressurePlates.cubePressurePlate(
        plateP,
        mass,
        new ICommand() {
          @Override
          public void execute() {
            System.out.println("EXECUTE");
            door.open();
          }

          @Override
          public void undo() {
            door.close();
          }
        });
  }

  public static Entity doorPressurePlateSphere(Point plateP, Point doorP, float mass) {
    DoorTile door = (DoorTile) Game.tileAt(doorP).orElseThrow();
    door.close();
    return PressurePlates.spherePressurePlate(
        plateP,
        mass,
        new ICommand() {
          @Override
          public void execute() {
            System.out.println("EXECUTE");
            door.open();
          }

          @Override
          public void undo() {
            door.close();
          }
        });
  }

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
              DialogUtils.showTextPopup("TBD", "Code Error");
            }
          }

          @Override
          public void undo() {
            if (cube != null) Game.remove(cube);
          }
        });
  }

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
              DialogUtils.showTextPopup("TBD", "Code Error");
            }
          }

          @Override
          public void undo() {
            if (sphere != null) Game.remove(sphere);
          }
        });
  }

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
              DialogUtils.showTextPopup("TBD", "Code Error");
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
              DialogUtils.showTextPopup("TBD", "Code Error");
            }
          }
        });
  }
}
