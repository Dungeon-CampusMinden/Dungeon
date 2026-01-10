package portal.level;

import contrib.hud.DialogUtils;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Map;
import portal.antiMaterialBarrier.AntiMaterialBarrier;
import portal.util.AdvancedLevel;

/** Level in the portal dungeon. */
public class SphereLevel_1 extends AdvancedLevel {

  private static final String NAME = "Portal Level";
  private static boolean showMsg = true;
  private static final String msg = "Neben Würfeln gibt es auch Kugeln.";
  private static final String task = "Suche den Code für die Kugel.";
  private static final String title = "Kubus";

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public SphereLevel_1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, NAME);
  }

  @Override
  protected void onFirstTick() {
    Game.add(LevelCreatorTools.doorPressurePlateSphere(getPoint("plate"), getPoint("door"), 3.5f));
    Game.add(AntiMaterialBarrier.antiMaterialBarrier(getPoint("amg"), true));

    Game.add(
        AntiMaterialBarrier.antiMaterialBarrier(
            getPoint("amg").translate(Vector2.of(0, -1)), true));
    Game.add(
        AntiMaterialBarrier.antiMaterialBarrier(
            getPoint("amg").translate(Vector2.of(0, -2)), true));

    Game.add(LevelCreatorTools.sphereSpawner(getPoint("spawner"), getPoint("cube")));

    if (showMsg)
      DialogUtils.showTextPopup(
          msg,
          title,
          () -> {
            showMsg = false;
            DialogUtils.showTextPopup(task, title);
          });
  }
}
