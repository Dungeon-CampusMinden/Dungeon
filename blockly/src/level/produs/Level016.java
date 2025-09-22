package level.produs;

import contrib.hud.DialogUtils;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/** In this level, simple backtracking techniques are used to find the correct path. */
public class Level016 extends BlocklyLevel {

  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level016(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 16");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Richtungen
        // Inventar und Charakter
        // Inventar und Charakter
        "wait",
        "drop_item",
        "push",
        "pull",
        "Items",
        // Kategorien
        // Bedingung
        "logic_monster_direction",
        "logic_breadcrumbs_direction",
        "logic_clover_direction",
        "logic_bossView_direction",
        "logic_switch_direction",
        "logic_active_direction",
        "logic_monster_direction",
        "logic_breadcrumbs_direction",
        "logic_clover_direction",
        "logic_bossView_direction",
        // Wahrheitsausdruecke
        "logic_operator",
        "usual_condition",
        // Kategorien
        "Variablen",
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    if (showText) {
      DialogUtils.showTextPopup("Ab jetzt wirds richtig schwer", "Bonus Level");
      showText = false;
    }
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.heroViewDirection(Direction.DOWN);
    LevelManagementUtils.zoomDefault();
  }

  @Override
  protected void onTick() {}
}
