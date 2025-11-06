package level.produs;

import static level.LevelManagementUtils.cameraFocusOn;

import contrib.hud.DialogUtils;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import entities.MiscFactory;
import entities.monster.BlocklyMonster;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, stones must be strategically pushed to block the monsters' line of sight. Only
 * when the player is not seen by a monster is it safe to step on the red tiles.
 */
public class Level005 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level005(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 5");
    this.blockBlocklyElement(
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
    LevelManagementUtils.playerViewDirection(Direction.RIGHT);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    Coordinate stone1C = getPoint(1).toCoordinate();
    Coordinate stone2C = getPoint(5).toCoordinate();
    Coordinate m1C = getPoint(0).toCoordinate();
    Coordinate m2C = getPoint(2).toCoordinate();
    Coordinate m3C = getPoint(3).toCoordinate();
    Coordinate m4C = getPoint(4).toCoordinate();

    Game.add(MiscFactory.stone(stone1C.toPoint()));
    Game.add(MiscFactory.stone(stone2C.toPoint()));

    BlocklyMonster.Builder guardBuilder = BlocklyMonster.GUARD.builder().addToGame();
    guardBuilder.attackRange(6);
    guardBuilder.viewDirection(Direction.LEFT);
    guardBuilder.build(m1C.toPoint());
    guardBuilder.attackRange(5);
    guardBuilder.viewDirection(Direction.RIGHT);
    guardBuilder.build(m2C.toPoint());
    guardBuilder.attackRange(5);
    guardBuilder.viewDirection(Direction.UP);
    guardBuilder.build(m3C.toPoint());
    guardBuilder.attackRange(5);
    guardBuilder.viewDirection(Direction.UP);
    guardBuilder.build(m4C.toPoint());
  }

  @Override
  protected void onTick() {}
}
