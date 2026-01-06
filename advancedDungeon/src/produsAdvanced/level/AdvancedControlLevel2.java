package produsAdvanced.level;

import contrib.hud.DialogUtils;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.Map;
import portal.util.AdvancedLevel;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Write your own movement controls.
 *
 * @see produsAdvanced.riddles.MyPlayerController
 */
public class AdvancedControlLevel2 extends AdvancedLevel {

  private static boolean showMsg = true;
  private static final String msg = "Hier geht es tief runter. Jetzt ganz langsam.";
  private static final String task = "Passe deinen Code so an, dass du dich langsamer bewegst.";
  private static final String title = "Level 2";

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public AdvancedControlLevel2(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Control");
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
