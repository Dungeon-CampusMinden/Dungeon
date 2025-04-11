package level.produs;

import contrib.hud.DialogUtils;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter11Level extends BlocklyLevel {

  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter11Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 1");
    this.blockBlocklyElement(
        "get_number",
        "repeat_number",
        "goToExit",
        "varNumber",
        "set_number_expression",
        "expression",
        "get_variable",
        "array_get",
        "direction_up",
        "direction_down",
        "direction_here",
        "item_breadcrumbs",
        "item_clover",
        "var_array",
        "array_set",
        "array_get",
        "array_length",
        "expression",
        "get_variable",
        "var_number",
        "repeat",
        "while_loop",
        "repeat_number",
        "get_number",
        "not_condition",
        "logic_operator",
        "logic_wall_direction",
        "get_number",
        "repeat_number",
        "logic_boolean",
        "usual_condition",
        "not_condition",
        "logic_operator",
        "logic_wall_direction",
        "logic_floor_direction",
        "logic_pit_direction",
        "logic_monster_direction",
        "logic_switch_direction",
        "logic_breadcrumbs_direction",
        "logic_clover_direction",
        "logic_active_direction",
        "controls_if",
        "controls_ifelse",
        "fireball",
        "wait",
        "use",
        "pickup",
        "push",
        "pull",
        "drop_item",
        "get_variable",
        "func_def",
        "func_call",
        "Schleife");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.heroViewDiretion(PositionComponent.Direction.DOWN);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      DialogUtils.showTextPopup(
          "Schau! Die Wache hat vergessen die Tür zu verriegeln Zeit für die Flucht. Lauf!",
          "Kapitel 1: Ausbruch");
      showText = false;
    }
  }

  @Override
  protected void onTick() {}
}
