package level.produs;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.MiscFactory;
import entities.monster.BlocklyMonster;
import java.util.*;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This level features the first boss fight. The player must shoot the boss three times to defeat
 * him.
 */
public class Level012 extends BlocklyLevel {

  private static boolean showText = true;
  private DoorTile door1, door2;
  private LeverComponent switch1, switch2, switch3, switch4;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level012(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 12");
    this.blockBlocklyElement(
        // Schleifen
        "while_loop",
        // Inventar und Charakter
        "drop_item",
        "Items",
        "wait",
        // Kategorien
        "Abfragen",
        "Bedingung",
        "Wahrheitsausdruecke",
        "Variablen",
        "Bedingungen",
        "Sonstige");

    addPopup(new ImagePopup("popups/level012/01_schaltplan.jpg"));
    addCodePopup(new ImagePopup("popups/overview1.jpg"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusOn(new Coordinate(10, 7));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.playerViewDirection(Direction.DOWN);
    Game.add(MiscFactory.stone(getPoint(1)));

    Entity s1 = LeverFactory.pressurePlate(getPoint(2));
    switch1 =
        s1.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s1);

    Game.add(MiscFactory.fireballScroll(getPoint(3)));
    Entity s2 = LeverFactory.pressurePlate(getPoint(4));
    switch2 =
        s2.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s2);
    Entity s3 = LeverFactory.createLever(getPoint(5));
    switch3 =
        s3.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s3);
    Entity s4 = LeverFactory.createLever(getPoint(6));
    switch4 =
        s4.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s4);

    Game.add(MiscFactory.fireballScroll(getPoint(8)));
    Game.add(MiscFactory.fireballScroll(getPoint(9)));

    BlocklyMonster.GUARD
        .builder()
        .attackRange(5)
        .viewDirection(Direction.LEFT)
        .addToGame()
        .build(getPoint(10));

    BlocklyMonster.BLACK_KNIGHT
        .builder()
        .attackRange(0)
        .addToGame()
        .viewDirection(Direction.UP)
        .onDeath(
            entity ->
                DialogUtils.showTextPopup("NEEEEEEEEEEEEEEEEIN! ICH WERDE MICH RÄCHEN!", "SIEG!"))
        .build(getPoint(11));

    door1 = (DoorTile) Game.tileAt(new Coordinate(4, 9)).orElse(null);
    door2 = (DoorTile) Game.tileAt(new Coordinate(14, 8)).orElse(null);
    door1.close();
    door2.close();

    if (showText) {
      showPopups();
      showText = false;
    }
  }

  @Override
  protected void onTick() {
    if (switch1.isOn()) door1.open();
    else door1.close();
    if (switch2.isOn() && switch4.isOn() && !switch3.isOn()) door2.open();
    else door2.close();
  }
}
