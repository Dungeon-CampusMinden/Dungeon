package level.produs;

import contrib.hud.DialogUtils;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import entities.MiscFactory;
import entities.monster.BlocklyMonster;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/** In this level, the fireball scrolls must be collected to defeat two of the three monsters. */
public class Level010 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level010(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 10");
    this.blockBlocklyElement(
        // Schleifen
        "while_loop",
        // Inventar und Charakter
        "drop_item",
        "Items",
        "wait",
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
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.heroViewDirection(Direction.LEFT);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      DialogUtils.showTextPopup(
          "Mit diesen Spruchrollen kannst du einen mächtigen Feuerball beschwören.",
          "Kapitel 1: Ausbruch");
      showText = false;
    }
    Game.add(MiscFactory.fireballScroll(getPoint(0)));
    Game.add(MiscFactory.fireballScroll(getPoint(1)));

    BlocklyMonster.Builder hedgehogBuilder =
        BlocklyMonster.HEDGEHOG.builder().attackRange(0).addToGame();
    hedgehogBuilder.build(getPoint(2));
    hedgehogBuilder.build(getPoint(3));
    hedgehogBuilder.build(getPoint(4));
  }

  @Override
  protected void onTick() {}
}
