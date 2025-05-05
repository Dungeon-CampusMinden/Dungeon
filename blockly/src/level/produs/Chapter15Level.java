package level.produs;

import static level.LevelManagementUtils.cameraFocusOn;

import contrib.hud.DialogUtils;
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

/** PRODUS LEVEL. */
public class Chapter15Level extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter15Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 5");
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
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.RIGHT);
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
    guardBuilder.viewDirection(PositionComponent.Direction.LEFT);
    guardBuilder.spawnPoint(m1C.toCenteredPoint());
    guardBuilder.build();
    guardBuilder.range(5);
    guardBuilder.viewDirection(PositionComponent.Direction.RIGHT);
    guardBuilder.spawnPoint(m2C.toCenteredPoint());
    guardBuilder.build();
    guardBuilder.range(5);
    guardBuilder.viewDirection(PositionComponent.Direction.UP);
    guardBuilder.spawnPoint(m2C.toCenteredPoint());
    guardBuilder.build();
    guardBuilder.range(5);
    guardBuilder.viewDirection(PositionComponent.Direction.UP);
    guardBuilder.spawnPoint(m2C.toCenteredPoint());
    guardBuilder.build();
  }

  @Override
  protected void onTick() {}
}
