package level.produs;

import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import entities.MiscFactory;
import entities.monster.BlocklyMonster;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This level builds on the previous one: fireball scrolls must be collected to defeat monsters.
 * Now, clever positioning is essential to succeed.
 */
public class Level010 extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level010(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 10");
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
    LevelManagementUtils.cameraFocusOn(new Coordinate(8, 6));
    LevelManagementUtils.heroViewDirection(Direction.RIGHT);
    Game.add(MiscFactory.fireballScroll(customPoints().get(0).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(1).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(2).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(3).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(4).toCenteredPoint()));

    BlocklyMonster.Builder guardBuilder = BlocklyMonster.GUARD.builder().attackRange(5).addToGame();
    guardBuilder.viewDirection(Direction.DOWN);
    guardBuilder.build(customPoints().get(5).toCenteredPoint());
    guardBuilder.build(customPoints().get(8).toCenteredPoint());
    guardBuilder.build(customPoints().get(9).toCenteredPoint());
    guardBuilder.viewDirection(Direction.RIGHT);
    guardBuilder.build(customPoints().get(6).toCenteredPoint());
    guardBuilder.build(customPoints().get(7).toCenteredPoint());
  }

  @Override
  protected void onTick() {}
}
