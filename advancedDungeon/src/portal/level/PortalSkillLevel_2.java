package portal.level;

import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.Map;
import portal.laserGrid.LaserGrid;
import portal.util.AdvancedLevel;

/** Level in the portal dungeon. */
public class PortalSkillLevel_2 extends AdvancedLevel {

  private static final String NAME = "Portal Level";
  private static final String msg = "Für dieses Level musst du beide Portale schießen können.";
  private static final String task = "Wechsel verlässlich zwischen den beiden Portalen.";
  private static final String title = "Portal Skill";
  private static boolean showMsg = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalSkillLevel_2(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, NAME);
  }

  @Override
  protected void onFirstTick() {
    Entity laser = LaserGrid.laserGrid(getPoint("laser"), false);
    Game.add(laser);
    Game.add(LevelCreatorTools.laserCubePlate(getPoint("cplate"), 10, laser));
    Game.add(LevelCreatorTools.doorPressurePlateSphere(getPoint("splate"), getPoint("door"), 3));
    Game.add(LevelCreatorTools.sphereSpawner(getPoint("sphereSpawner"), getPoint("sphere")));
    Game.add(LevelCreatorTools.cubeSpawner(getPoint("cubeSpawner"), getPoint("cube")));
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
