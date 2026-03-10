package level.produs;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
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
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, both levers and stones must be used. The wiring diagram shows the Boolean logic
 * connections between doors and levers.
 */
public class Level008 extends BlocklyLevel {
  private static boolean showText = true;
  private LeverComponent switch1, switch2;
  private DoorTile door1, door2, door3, door4;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level008(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 8");
    this.blockBlocklyElement(
        // Schleifen
        "while_loop",
        // Inventar und Charakter
        "fireball",
        "wait",
        "pickup",
        "drop_item",
        "Items",
        // Kategorien
        "Abfragen",
        "Bedingung",
        "Wahrheitsausdruecke",
        "Variablen",
        "Bedingungen",
        "Sonstige");

    addPopup(new ImagePopup("popups/level008/01_schaltplan.jpg"));

    addCodePopup(new ImagePopup("popups/overview1.jpg"));
  }

  @Override
  protected void onFirstTick() {

    LevelManagementUtils.fog(false);
    if (showText) {
      showPopups();
      showText = false;
    }
    LevelManagementUtils.cameraFocusOn(new Coordinate(8, 8));
    LevelManagementUtils.playerViewDirection(Direction.RIGHT);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();

    Entity s1 = LeverFactory.pressurePlate(getPoint(1));
    Entity s2 = LeverFactory.createLever(getPoint(2));

    switch1 =
        s1.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    switch2 =
        s2.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s2, LeverComponent.class));

    Game.add(s1);
    Game.add(s2);

    door1 = (DoorTile) Game.tileAt(new Coordinate(7, 8)).orElse(null);
    door1.close();
    door2 = (DoorTile) Game.tileAt(new Coordinate(12, 10)).orElse(null);
    door2.close();
    door3 = (DoorTile) Game.tileAt(new Coordinate(12, 5)).orElse(null);
    door3.close();
    door4 = (DoorTile) Game.tileAt(new Coordinate(10, 2)).orElse(null);
    door4.close();

    Game.add(MiscFactory.stone(getPoint(0)));
  }

  @Override
  protected void onTick() {
    if (switch1.isOn()) door1.open();
    else door1.close();
    if (!switch1.isOn()) door2.open();
    else door2.close();
    if (!switch2.isOn()) door3.open();
    else door3.close();
    if (switch2.isOn() && switch1.isOn()) door4.open();
    else door4.close();
  }
}
