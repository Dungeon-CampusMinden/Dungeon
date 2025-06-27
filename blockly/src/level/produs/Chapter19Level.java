package level.produs;

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
public class Chapter19Level extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter19Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kaptitel 1: Level 9");
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
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.LEFT);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      DialogUtils.showTextPopup(
          "Mit diesen Spruchrollen kannst du einen mächtigen Feuerball beschwören.",
          "Kapitel 1: Ausbruch");
      showText = false;
    }
    Game.add(MiscFactory.fireballScroll(customPoints().get(0).toCenteredPoint()));
    Game.add(MiscFactory.fireballScroll(customPoints().get(1).toCenteredPoint()));

    BlocklyMonster.BlocklyMonsterBuilder hedgehogBuilder = BlocklyMonster.HEDGEHOG.builder();
    hedgehogBuilder.range(0);
    hedgehogBuilder.addToGame();
    hedgehogBuilder.spawnPoint(customPoints().get(2).toCenteredPoint());
    hedgehogBuilder.build();
    hedgehogBuilder.spawnPoint(customPoints().get(3).toCenteredPoint());
    hedgehogBuilder.build();
    hedgehogBuilder.spawnPoint(customPoints().get(4).toCenteredPoint());
    hedgehogBuilder.build();
  }

  @Override
  protected void onTick() {}
}
