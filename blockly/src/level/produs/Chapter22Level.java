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
public class Chapter22Level extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter22Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 2: Level 2");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Richtungen
        "direction_here",
        // Inventar und Charakter
        "wait",
        "drop_item",
        "Items",
        // Bedingung
        "logic_pit_direction",
        "logic_monster_direction",
        "logic_breadcrumbs_direction",
        "logic_clover_direction",
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
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusOn(new Coordinate(5, 8));
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.UP);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      DialogUtils.showTextPopup(
          "Ganz schön verwirrend hier. Du brauchst eine gute Strategie um den Ausgang zu finden.",
          "Kapitel 2: Flucht");
      showText = false;
    }
  }

  @Override
  protected void onTick() {}
}
