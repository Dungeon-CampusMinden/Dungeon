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
import entities.monster.BlocklyMonster;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, both levers and stones must be used. The wiring diagram shows the Boolean logic
 * connections between doors and levers.
 */
public class Level009 extends BlocklyLevel {

  private LeverComponent switch1, switch2, switch3;
  private DoorTile door1, door2;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level009(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> customPoints) {
    super(layout, designLabel, customPoints, "Level 9");
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

    addWebPopup(new ImagePopup("popups/level009/Feuerball.png"));
    addWebPopup(new ImagePopup("popups/level009/Feuerball2.png"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusOn(new Coordinate(12, 4));
    LevelManagementUtils.playerViewDirection(Direction.DOWN);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    showPopups();
    Game.add(MiscFactory.fireballScroll(getPoint(0)));
    Game.add(MiscFactory.fireballScroll(getPoint(1)));

    showPopups();
    Game.add(MiscFactory.fireballScroll(getPoint(0)));
    Game.add(MiscFactory.fireballScroll(getPoint(1)));

    BlocklyMonster.Builder guardBuilder = BlocklyMonster.GUARD.builder().addToGame();
    guardBuilder.attackRange(5);
    guardBuilder.viewDirection(Direction.UP);
    guardBuilder.build(getPoint(4));
    guardBuilder.attackRange(5);
    guardBuilder.viewDirection(Direction.LEFT);
    guardBuilder.build(getPoint(5));

    Entity s1 = LeverFactory.createLever(getPoint(1));
    Entity s2 = LeverFactory.createLever(getPoint(2));
    Entity s3 = LeverFactory.createLever(getPoint(3));

    switch1 =
        s1.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    switch2 =
        s2.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s2, LeverComponent.class));
    switch3 =
        s3.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s3, LeverComponent.class));

    Game.add(s1);
    Game.add(s2);
    Game.add(s3);

    door1 = (DoorTile) Game.tileAt(new Coordinate(10, 8)).orElse(null);
    door1.close();
    door2 = (DoorTile) Game.tileAt(new Coordinate(8, 5)).orElse(null);
    door2.close();

    Game.add(MiscFactory.stone(getPoint(0)));
  }

  @Override
  protected void onTick() {
    if ((switch1.isOn() ^ switch2.isOn() && switch3.isOn())) door2.open();
    else door2.close();
  }
}
