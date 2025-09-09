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
import core.utils.components.MissingComponentException;
import entities.MiscFactory;
import entities.monster.BlocklyMonster;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This level features the first boss fight. The player must shoot the boss three times to defeat
 * him.
 */
public class Level011 extends BlocklyLevel {

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
  public Level011(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 11");
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
    LevelManagementUtils.cameraFocusOn(new Coordinate(10, 7));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.heroViewDirection(Direction.DOWN);
    if (showText) {
      DialogUtils.showTextPopup(
          "Hahahaha! An MIR kommst du NIE vorbei. GIB AUF!", "BOSS: Der Wärter");
      showText = false;
    }

    Game.add(MiscFactory.stone(customPoints().get(1).toPoint()));

    Entity s1 = LeverFactory.pressurePlate(customPoints().get(2).toPoint());
    switch1 =
        s1.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s1);

    Game.add(MiscFactory.fireballScroll(customPoints().get(3).toPoint()));
    Entity s2 = LeverFactory.pressurePlate(customPoints().get(4).toPoint());
    switch2 =
        s2.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s2);
    Entity s3 = LeverFactory.createLever(customPoints().get(5).toPoint());
    switch3 =
        s3.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s3);
    Entity s4 = LeverFactory.createLever(customPoints().get(6).toPoint());
    switch4 =
        s4.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    Game.add(s4);

    Game.add(MiscFactory.fireballScroll(customPoints().get(8).toPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(9).toPoint()));

    BlocklyMonster.GUARD
        .builder()
        .attackRange(5)
        .viewDirection(Direction.LEFT)
        .addToGame()
        .build(customPoints().get(10));

    BlocklyMonster.BLACK_KNIGHT
        .builder()
        .attackRange(0)
        .addToGame()
        .viewDirection(Direction.UP)
        .onDeath(
            entity ->
                DialogUtils.showTextPopup("NEEEEEEEEEEEEEEEEIN! ICH WERDE MICH RÄCHEN!", "SIEG!"))
        .build(customPoints().get(11));

    door1 = (DoorTile) Game.tileAt(new Coordinate(4, 9)).orElse(null);
    door2 = (DoorTile) Game.tileAt(new Coordinate(14, 8)).orElse(null);
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
