package level.produs;

import components.AmmunitionComponent;
import contrib.hud.DialogUtils;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.MissingHeroException;
import entities.monster.BlocklyMonster;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This level extends simple backtracking by adding monsters to the maze. The player must navigate
 * carefully while avoiding or dealing with monsters.
 */
public class Level018 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level018(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 18");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Inventar und Charakter
        // Variable
        "get_number",
        // Bedingung
        "logic_bossView_direction",
        // Kategorien
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    if (showText) {
      DialogUtils.showTextPopup(
          "Ich geb dir ein paar Feuerballspruchrollen. Viel Erfolg!", "Kapitel 3: Rache");
      showText = false;
    }
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.heroViewDirection(Direction.LEFT);
    BlocklyMonster.Builder hedgehogBuilder =
        BlocklyMonster.HEDGEHOG.builder().attackRange(0).addToGame();
    Game.hero()
        .orElseThrow(MissingHeroException::new)
        .fetch(AmmunitionComponent.class)
        .orElseThrow()
        .currentAmmunition(20);

    customPoints()
        .forEach(
            coordinate -> {
              hedgehogBuilder.build(coordinate.toPoint());
            });
  }

  @Override
  protected void onTick() {}
}
