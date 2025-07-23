package level.produs;

import static level.LevelManagementUtils.cameraFocusOn;

import contrib.hud.DialogUtils;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import entities.BlocklyMonster;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, stones must be strategically pushed to block the monsters' line of sight. Only
 * when the hero is not seen by a monster is it safe to step on the red tiles.
 */
public class Level005 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level005(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 5");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Richtungen
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
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    if (showText) {
      DialogUtils.showTextPopup(
          "Die Monster sehen böse aus, du solltest ihnen nicht zu nahe kommen.",
          "Kapitel 1: Ausbruch");
      showText = false;
    }
    cameraFocusOn(new Coordinate(7, 6));
    LevelManagementUtils.heroViewDirection(Direction.RIGHT);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    Coordinate stone1C = customPoints().get(1);
    Coordinate stone2C = customPoints().get(5);
    Coordinate m1C = customPoints().get(0);
    Coordinate m2C = customPoints().get(2);
    Coordinate m3C = customPoints().get(3);
    Coordinate m4C = customPoints().get(4);

    Game.add(MiscFactory.stone(stone1C.toCenteredPoint()));
    Game.add(MiscFactory.stone(stone2C.toCenteredPoint()));

    BlocklyMonster.BlocklyMonsterBuilder guardBuilder = BlocklyMonster.GUARD.builder();
    guardBuilder.addToGame();
    guardBuilder.range(6);
    guardBuilder.viewDirection(Direction.LEFT);
    guardBuilder.spawnPoint(m1C.toCenteredPoint());
    guardBuilder.build();
    guardBuilder.range(5);
    guardBuilder.viewDirection(Direction.RIGHT);
    guardBuilder.spawnPoint(m2C.toCenteredPoint());
    guardBuilder.build();
    guardBuilder.range(5);
    guardBuilder.viewDirection(Direction.UP);
    guardBuilder.spawnPoint(m3C.toCenteredPoint());
    guardBuilder.build();
    guardBuilder.range(5);
    guardBuilder.viewDirection(Direction.UP);
    guardBuilder.spawnPoint(m4C.toCenteredPoint());
    guardBuilder.build();
  }

  @Override
  protected void onTick() {}
}
