package level.produs;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.components.MissingComponentException;
import entities.BlocklyMonster;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, both levers and stones must be used. The wiring diagram shows the Boolean logic
 * connections between doors and levers.
 */
public class Level008 extends BlocklyLevel {

  private LeverComponent switch1, switch2, switch3, switch4, switch5;
  private DoorTile door1, door2, door3, door4, door5;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level008(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 8");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Richtungen
        // Schleifen
        "while_loop",
        // Inventar und Charakter
        "drop_item",
        "Items",
        // Kategorien
        "Abfragen",
        "Bedingung",
        "Wahrheitsausdruecke",
        "Variablen",
        "Bedingungen",
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.heroViewDirection(Direction.RIGHT);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.zoomDefault();

    BlocklyMonster.BlocklyMonsterBuilder guardBuilder = BlocklyMonster.GUARD.builder();
    guardBuilder.addToGame();
    guardBuilder.range(5);
    guardBuilder.viewDirection(Direction.UP);
    guardBuilder.spawnPoint(customPoints().get(7).toCenteredPoint());
    guardBuilder.build();
    guardBuilder.range(5);
    guardBuilder.viewDirection(Direction.LEFT);
    guardBuilder.spawnPoint(customPoints().get(8).toCenteredPoint());
    guardBuilder.build();

    Entity s1 = MiscFactory.pressurePlate(customPoints().get(2).toCenteredPoint());
    Entity s2 = LeverFactory.createLever(customPoints().get(3).toCenteredPoint());
    Entity s3 = LeverFactory.createLever(customPoints().get(4).toCenteredPoint());
    Entity s4 = LeverFactory.createLever(customPoints().get(5).toCenteredPoint());
    Entity s5 = LeverFactory.createLever(customPoints().get(6).toCenteredPoint());
    switch1 =
        s1.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    switch2 =
        s2.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s2, LeverComponent.class));
    switch3 =
        s3.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s3, LeverComponent.class));
    switch4 =
        s4.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s4, LeverComponent.class));
    switch5 =
        s5.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s5, LeverComponent.class));
    Game.add(s1);
    Game.add(s2);
    Game.add(s3);
    Game.add(s4);
    Game.add(s5);

    door1 = (DoorTile) Game.tileAT(new Coordinate(7, 15));
    door1.close();

    door2 = (DoorTile) Game.tileAT(new Coordinate(12, 17));
    door2.close();
    door3 = (DoorTile) Game.tileAT(new Coordinate(12, 12));
    door3.close();
    door4 = (DoorTile) Game.tileAT(new Coordinate(10, 9));
    door4.close();
    door5 = (DoorTile) Game.tileAT(new Coordinate(8, 5));
    door5.close();

    Game.add(MiscFactory.stone(customPoints().get(0).toCenteredPoint()));
    Game.add(MiscFactory.stone(customPoints().get(1).toCenteredPoint()));
  }

  @Override
  protected void onTick() {
    Coordinate heroCoord = EntityUtils.getHeroCoordinate();
    if (heroCoord == null) {
      return; // Hero not yet spawned
    }
    if (heroCoord.y() < 10) LevelManagementUtils.cameraFocusOn(new Coordinate(10, 5));
    else LevelManagementUtils.cameraFocusOn(new Coordinate(10, 15));

    if (switch1.isOn()) door1.open();
    else door1.close();

    if (!switch1.isOn()) door2.open();
    else door2.close();
    if (!switch2.isOn()) door3.open();
    else door3.close();
    if (switch2.isOn() && switch1.isOn()) door4.open();
    else door4.close();

    if ((switch3.isOn() ^ switch4.isOn() && switch5.isOn())) door5.open();
    else door5.close();
  }
}
