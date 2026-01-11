package portal.level;

import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Map;
import portal.antiMaterialBarrier.AntiMaterialBarrier;
import portal.laserGrid.LaserGrid;
import portal.util.AdvancedLevel;

/** Level in the portal dungeon. */
public class AntiMaterialLevel_1 extends AdvancedLevel {

  private static final String NAME = "Portal Level";

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public AntiMaterialLevel_1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, NAME);
  }

  @Override
  protected void onFirstTick() {
    Game.add(LevelCreatorTools.doorPressurePlateSphere(getPoint("plate"), getPoint("door"), 12f));
    Game.add(LevelCreatorTools.sphereSpawner(getPoint("lever"), getPoint("sphere")));
    Game.add(AntiMaterialBarrier.antiMaterialBarrier(getPoint("amg"), true));
    Game.add(
        AntiMaterialBarrier.antiMaterialBarrier(
            getPoint("amg").translate(Vector2.of(0, -1)), true));
    Game.add(
        AntiMaterialBarrier.antiMaterialBarrier(
            getPoint("amg").translate(Vector2.of(0, -2)), true));
    Game.add(
        AntiMaterialBarrier.antiMaterialBarrier(
            getPoint("amg").translate(Vector2.of(0, -3)), true));
    Game.add(LaserGrid.laserGrid(getPoint("laser"), false));
    Game.add(LaserGrid.laserGrid(getPoint("laser").translate(Vector2.of(1, 0)), false));
    Game.add(LaserGrid.laserGrid(getPoint("laser").translate(Vector2.of(2, 0)), false));
  }

  @Override
  protected void onTick() {}
}
