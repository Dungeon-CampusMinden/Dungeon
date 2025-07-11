package level.produs;

import contrib.hud.DialogUtils;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.BlocklyMonster;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, monsters are scattered across the map. The hero must avoid them by navigating
 * carefully. Stepping on red tiles or touching a monster will result in failure.
 */
public class Level002 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level002(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 2");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Richtungen
        "direction_up",
        "direction_down",
        "direction_here",
        // Kategorien
        "Inventar & Charakter",
        "Abfragen",
        "Bedingung",
        "Wahrheitsausdruecke",
        "Variablen",
        "Schleife",
        "Bedingungen",
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.RIGHT);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      DialogUtils.showTextPopup(
          "Pass auf, die Monster sind angekettet und können sich nicht bewegen, aber wenn du ihnen zu nahe kommst, wird es eng für dich.",
          "Kapitel 1: Ausbruch");
      showText = false;
    }

    BlocklyMonster.BlocklyMonsterBuilder guardBuilder = BlocklyMonster.GUARD.builder();
    guardBuilder.range(3);
    guardBuilder.viewDirection(PositionComponent.Direction.LEFT);
    guardBuilder.addToGame();
    guardBuilder.spawnPoint(customPoints().get(0).toCenteredPoint());
    guardBuilder.build().orElseThrow();
    customPoints().remove(0);

    BlocklyMonster.BlocklyMonsterBuilder hedgehogBuilder = BlocklyMonster.HEDGEHOG.builder();
    hedgehogBuilder.range(0);
    customPoints()
        .forEach(
            coordinate -> {
              hedgehogBuilder.spawnPoint(coordinate.toCenteredPoint());
              hedgehogBuilder.addToGame();
              hedgehogBuilder.build();
            });
  }

  @Override
  protected void onTick() {}
}
