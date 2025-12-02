package level.produs;

import components.AmmunitionComponent;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.MissingPlayerException;
import core.utils.Point;
import entities.monster.BlocklyMonster;
import java.util.Map;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * This level extends simple backtracking by adding monsters to the maze. The player must navigate
 * carefully while avoiding or dealing with monsters.
 */
public class Level019 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public Level019(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Level 19");
    this.blockBlocklyElement(
        // Inventar und Charakter
        "drop_item",
        "Items",
        "wait",
        // Variable
        "get_number",
        "switch_case",
        "case_block",
        "default_block",
        // Bedingung
        "logic_bossView_direction",
        // Kategorien
        "Sonstige");

    addWebPopup(new ImagePopup("popups/level019/01_intro.png"));
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    if (showText) {
      showPopups();
      showText = false;
    }
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.playerViewDirection(Direction.LEFT);
    BlocklyMonster.Builder hedgehogBuilder =
        BlocklyMonster.HEDGEHOG.builder().attackRange(0).addToGame();
    Game.player()
        .orElseThrow(MissingPlayerException::new)
        .fetch(AmmunitionComponent.class)
        .orElseThrow()
        .currentAmmunition(20);

    namedPoints()
        .forEach(
            (name, point) -> {
              hedgehogBuilder.build(point);
            });
  }

  @Override
  protected void onTick() {}
}
