package level.produs;

import contrib.hud.DialogUtils;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/** PRODUS LEVEL. */
public class Chapter25Level extends BlocklyLevel {

  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter25Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 2: Level 5");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Inventar und Charakter
        "wait",
        // Item
        "item_clover",
        // Bedingung
        "logic_pit_direction",
        "logic_clover_direction",
        // Variable
        "get_number",
        // Kategorien
        // "Sonstige");
        "func_def",
        "func_call",
        "var_array",
        "array_set",
        "array_get",
        "array_length");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    if (showText) {
      DialogUtils.showTextPopup(
          "Nutz deinen Beutel mit Krumen, um deinen Weg hier raus zu finden.", "Kapitel 2: Flucht");
      showText = false;
    }
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.DOWN);
    LevelManagementUtils.zoomDefault();
  }

  @Override
  protected void onTick() {}
}
