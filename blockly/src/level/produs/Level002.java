package level.produs;

import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import entities.monster.BlocklyMonster;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, monsters are scattered across the map. The player must avoid them by navigating
 * carefully. Stepping on red tiles or touching a monster will result in failure.
 */
public class Level002 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level002(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 2");
    this.blockBlocklyElement(
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

    addWebPopup(new ImagePopup("popups/level002/01_intro.png"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.playerViewDirection(Direction.RIGHT);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      showPopups();
      showText = false;
    }

    BlocklyMonster.GUARD
        .builder()
        .attackRange(3)
        .viewDirection(Direction.LEFT)
        .addToGame()
        .build(getPoint("guard"));

    removeNamedPoint("guard");

    BlocklyMonster.Builder hedgehogBuilder = BlocklyMonster.HEDGEHOG.builder().attackRange(0);
    namedPoints()
        .forEach(
            (name, point) -> {
              hedgehogBuilder.addToGame().build(point);
            });
  }

  @Override
  protected void onTick() {}
}
