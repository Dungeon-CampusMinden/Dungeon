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
 * In this level, nested for-loops can be used to create a compact and efficient solution. The
 * player must push two stones onto pressure plates to unlock the exit.
 */
public class Level006 extends BlocklyLevel {
  private DoorTile door;
  private LeverComponent switch1, switch2;
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level006(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 6");
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

    addCodePopup(new ImagePopup("popups/codepopups/level006/01_intro.png"));
    addCodePopup(new ImagePopup("popups/codepopups/level006/02_intro.png"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.playerViewDirection(Direction.RIGHT);
    LevelManagementUtils.zoomDefault();
    Coordinate stone1C = getPoint(0).toCoordinate();
    Coordinate stone2C = getPoint(1).toCoordinate();
    Coordinate switch1C = getPoint(2).toCoordinate();
    Coordinate switch2C = getPoint(3).toCoordinate();
    Entity s1 = LeverFactory.pressurePlate(switch1C.toPoint());
    Entity s2 = LeverFactory.pressurePlate(switch2C.toPoint());
    Game.add(MiscFactory.stone(stone1C.toPoint()));
    Game.add(MiscFactory.stone(stone2C.toPoint()));
    if (showText) {
      showPopups();
      showText = false;
    }
    Game.add(s1);
    Game.add(s2);
    switch1 =
        s1.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    switch2 =
        s2.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s2, LeverComponent.class));
    door = (DoorTile) Game.tileAt(new Coordinate(5, 12)).orElse(null);
    door.close();
  }

  @Override
  protected void onTick() {
    if (switch1.isOn() && switch2.isOn()) door.open();
    else door.close();
  }
}
