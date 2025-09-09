package level.produs;

import contrib.hud.DialogUtils;
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

/** In this level, the fireball scrolls must be collected to defeat two of the three monsters. */
public class Level009 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level009(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 9");
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
    Game.add(MiscFactory.fireballScroll(customPoints().get(0).toPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(1).toPoint()));

    BlocklyMonster.Builder hedgehogBuilder =
        BlocklyMonster.HEDGEHOG.builder().attackRange(0).addToGame();
    hedgehogBuilder.build(customPoints().get(2).toPoint());
    hedgehogBuilder.build(customPoints().get(3).toPoint());
    hedgehogBuilder.build(customPoints().get(4).toPoint());
  }

  @Override
  protected void onTick() {}
}
