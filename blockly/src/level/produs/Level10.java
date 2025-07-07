package level.produs;

import core.Game;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.BlocklyMonster;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This level builds on the previous one: fireball scrolls must be collected to defeat monsters.
 * Now, clever positioning is essential to succeed.
 */
public class Level10 extends BlocklyLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level10(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 10");
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
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.RIGHT);
    Game.add(MiscFactory.fireballScroll(customPoints().get(0).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(1).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(2).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(3).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(4).toCenteredPoint()));

    BlocklyMonster.BlocklyMonsterBuilder guardBuilder = BlocklyMonster.GUARD.builder();
    guardBuilder.addToGame();
    guardBuilder.range(5);
    guardBuilder.viewDirection(PositionComponent.Direction.DOWN);
    guardBuilder.spawnPoint(customPoints().get(5).toCenteredPoint());
    guardBuilder.build();
    guardBuilder.spawnPoint(customPoints().get(8).toCenteredPoint());
    guardBuilder.build();
    guardBuilder.spawnPoint(customPoints().get(9).toCenteredPoint());
    guardBuilder.build();
    guardBuilder.viewDirection(PositionComponent.Direction.RIGHT);
    guardBuilder.spawnPoint(customPoints().get(6).toCenteredPoint());
    guardBuilder.build();
    guardBuilder.spawnPoint(customPoints().get(7).toCenteredPoint());
    guardBuilder.build();
  }

  @Override
  protected void onTick() {}
}
