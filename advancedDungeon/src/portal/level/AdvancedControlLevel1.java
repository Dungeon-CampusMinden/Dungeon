package portal.level;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import java.util.Optional;

import portal.laser.LaserCube;
import portal.laser.LaserFactory;
import portal.laser.LaserReceiver;
import portal.laser.LaserUtil;
import portal.riddles.MyPlayerController;
import portal.util.AdvancedLevel;
import portal.util.ToggleableComponent;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Write your own movement controls.
 *
 * @see MyPlayerController
 */
public class AdvancedControlLevel1 extends AdvancedLevel {

  private static boolean showMsg = true;
  private static String msg =
      "Was ist los? Ich kann mich nicht bewegen! Jemand muss an meinem Steuerungscode rumgefuscht haben.";
  private static String task =
      "Gehe in die Datei MyPlayerController.java und implementiere die Steuerung deines Helden.\n";
  private static String title = "Level 1";

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public AdvancedControlLevel1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Control");
  }

  Entity lever;
  Entity laserReceiver;

  @Override
  protected void onFirstTick() {
    if (showMsg)
      DialogUtils.showTextPopup(
          msg,
          title,
          () -> {
            showMsg = false;
            DialogUtils.showTextPopup(task, title);
          });

    Entity laser = LaserFactory.createLaser(namedPoints().get("door1"), Direction.DOWN);

    laserReceiver = LaserReceiver.laserReceiver(namedPoints().get("door2"));

//    LaserUtil.activate(laser);
//    LaserUtil.activate(laserReceiver);


    Entity localLever =
      LeverFactory.createLever(
        new Point(9, 3),
        new ICommand() {
          @Override
          public void execute() {
            LaserUtil.activate(laser);
          }

          @Override
          public void undo() {
            LaserUtil.deactivate(laser);
          }
        });



    Game.add(laser);
    Game.add(laserReceiver);
    Game.add(localLever);

    Entity cube = LaserCube.laserCube(namedPoints().get("door4"), Direction.UP);
    Game.add(cube);
    Entity cube2 = LaserCube.laserCube(namedPoints().get("door5"), Direction.UP);
    Game.add(cube2);
    Entity cube3 = LaserCube.laserCube(namedPoints().get("door6"), Direction.UP);
    Game.add(cube3);
    Entity cube4 = LaserCube.laserCube(namedPoints().get("door7"), Direction.UP);
    Game.add(cube4);
    Entity cube5 = LaserCube.laserCube(namedPoints().get("door8"), Direction.UP);
    Game.add(cube5);
//    Entity cube = LaserCube.laserCube(namedPoints().get("door4"), Direction.UP);
//    Game.add(cube);
//    Entity cube = LaserCube.laserCube(namedPoints().get("door4"), Direction.UP);
//    Game.add(cube);
//    laserReceiver.fetch(ToggleableComponent.class).


  }

  @Override
  protected void onTick() {
    boolean active = laserReceiver.fetch(ToggleableComponent.class).get().isActive();

    if (active) {
      System.out.println("is active");
    } else {
      System.out.println("is not active");
    }

  }
}
