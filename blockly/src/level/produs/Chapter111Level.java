package level.produs;

import contrib.components.HealthComponent;
import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.MissingComponentException;
import entities.BlocklyMonster;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/** PRODUS LEVEL. */
public class Chapter111Level extends BlocklyLevel {

  private static boolean showText = true;
  private DoorTile door1, door2;
  private LeverComponent switch1, switch2, switch3, switch4;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter111Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 11");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Richtungen
        "direction_up",
        "direction_down",
        "direction_here",
        // Schleifen
        "while_loop",
        // Inventar und Charakter
        "wait",
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
    LevelManagementUtils.cameraFocusOn(new Coordinate(10, 7));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.DOWN);
    if (showText) {
      DialogUtils.showTextPopup(
          "Hahahaha! An MIR kommst du NIE vorbei. GIB AUF!", "BOSS: Der Wärter");
      showText = false;
    }

    Game.add(MiscFactory.stone(customPoints().get(1).toCenteredPoint()));

    Entity s1 = MiscFactory.pressurePlate(customPoints().get(2).toCenteredPoint());
    switch1 =
        s1.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s1);

    Game.add(MiscFactory.fireballScroll(customPoints().get(3).toCenteredPoint()));
    Entity s2 = MiscFactory.pressurePlate(customPoints().get(4).toCenteredPoint());
    switch2 =
        s2.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s2);
    Entity s3 = LeverFactory.createLever(customPoints().get(5).toCenteredPoint());
    switch3 =
        s3.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s3);
    Entity s4 = LeverFactory.createLever(customPoints().get(6).toCenteredPoint());
    switch4 =
        s4.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s4);

    Game.add(MiscFactory.fireballScroll(customPoints().get(8).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(9).toCenteredPoint()));

    BlocklyMonster.BlocklyMonsterBuilder guardBuilder = BlocklyMonster.GUARD.builder();
    guardBuilder.addToGame();
    guardBuilder.range(5);
    guardBuilder.viewDirection(PositionComponent.Direction.LEFT);
    guardBuilder.spawnPoint(customPoints().get(10).toCenteredPoint());
    guardBuilder.build();

    BlocklyMonster.BlocklyMonsterBuilder bossBuilder = BlocklyMonster.BLACK_KNIGHT.builder();
    bossBuilder.range(0);
    bossBuilder.addToGame();
    bossBuilder.viewDirection(PositionComponent.Direction.UP);
    bossBuilder.spawnPoint(customPoints().get(11).toCenteredPoint());
    Entity boss = bossBuilder.build().orElseThrow();
    boss.fetch(HealthComponent.class)
        .orElseThrow()
        .onDeath(
            entity -> {
              DialogUtils.showTextPopup("NEEEEEEEEEEEEEEEEIN! ICH WERDE MICH RÄCHEN!", "SIEG!");
            });
    door1 = (DoorTile) Game.tileAT(new Coordinate(4, 9));
    door2 = (DoorTile) Game.tileAT(new Coordinate(14, 8));
    door1.close();
    door2.close();
  }

  @Override
  protected void onTick() {
    if (switch1.isOn()) door1.open();
    else door1.close();
    if (switch2.isOn() && switch4.isOn() && !switch3.isOn()) door2.open();
    else door2.close();
  }
}
