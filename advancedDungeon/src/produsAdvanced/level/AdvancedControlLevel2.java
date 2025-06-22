package produsAdvanced.level;

import contrib.entities.SignFactory;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import core.utils.Point;
import level.AdvancedLevel;

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
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public AdvancedControlLevel2(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Control");
  }

  @Override
  protected void onFirstTick() {
    if (showMsg) DialogUtils.showTextPopup(msg, title, () -> {
      showMsg = false;
      DialogUtils.showTextPopup(task, title);
    });
  }

  @Override
  protected void onTick() {}
}
