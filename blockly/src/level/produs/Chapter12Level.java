package level.produs;

import contrib.hud.DialogUtils;
import core.Game;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.MissingHeroException;
import entities.BlocklyMonsterFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/** PRODUS LEVEL. */
public class Chapter12Level extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter12Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 2");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Richtungen
        "direction_up",
        "direction_down",
        "direction_here",
        // Kategorien
        /*"Inventar & Charakter",
        "Abfragen",
        "Bedingung",
        "Wahrheitsausdruecke",
        "Variablen",
        "Schleife",
        "Bedingungen",
        "Sonstige",*/
        "fireball",
        "wait",
        "use",
        "pickup",
        "push",
        "pull",
        "drop_item",
        "item_breadcrumbs",
        "item_clover",
        "controls_if",
        "controls_ifelse",
        "logic_wall_direction",
        "logic_floor_direction",
        "logic_pit_direction",
        "logic_monster_direction",
        "logic_switch_direction",
        "logic_breadcrumbs_direction",
        "logic_clover_direction",
        "logic_active_direction",
        "logic_boolean",
        "usual_condition",
        "not_condition",
        "logic_operator",
        "var_number",
        "set_number_expression",
        "expression",
        "get_variable",
        "get_number",
        "repeat",
        "while_loop",
        "repeat_number",
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
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.RIGHT);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      DialogUtils.showTextPopup(
          "Pass auf, die Monster sind angekettet und können sich nicht bewegen, aber wenn du ihnen zu nahe kommst, wird es eng für dich.",
          "Kapitel 1: Ausbruch");
      showText = false;
    }
    Game.add(
        BlocklyMonsterFactory.guard(customPoints().get(0), PositionComponent.Direction.LEFT, 3));
    customPoints().remove(0);
    customPoints().forEach(coordinate -> Game.add(BlocklyMonsterFactory.hedgehog(coordinate)));

    System.out.println(
        "View direction after first tick "
            + Game.hero()
                .orElseThrow(MissingHeroException::new)
                .fetch(PositionComponent.class)
                .orElseThrow()
                .viewDirection());
  }

  @Override
  protected void onTick() {}
}
