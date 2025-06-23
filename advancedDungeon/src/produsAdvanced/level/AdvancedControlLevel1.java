package produsAdvanced.level;

import contrib.hud.DialogUtils;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;
import level.AdvancedLevel;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Write your own movement controls.
 *
 * @see produsAdvanced.riddles.MyPlayerController
 */
public class AdvancedControlLevel1 extends AdvancedLevel {

  private static boolean showMsg = true;
  private static String msg =
      "Was ist los? Ich kann mich nicht bewegen! Jemand muss an meinem Steuerungscode rumgefuscht haben.";
  private static String task =
      "Gehe in die Datei MyPlayerController.java und implementiere die Steuerung deines Helden.\n";
  private static String title = "Level 1";

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public AdvancedControlLevel1(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Control");
  }

  @Override
  protected void onFirstTick() {
    if (showMsg)
      DialogUtils.showTextPopup(
          msg,
          title,
          () -> {
            showMsg = false;
            DialogUtils.showTextPopup(task, title);
          });
  }

  @Override
  protected void onTick() {}
}
