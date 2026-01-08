package portal.level;

import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Map;
import portal.antiMaterialBarrier.AntiMaterialBarrier;
import portal.portals.PortalColor;
import portal.portals.PortalFactory;
import portal.util.AdvancedLevel;

/** Level in the portal dungeon. */
public class PortalSkillLevel_1 extends AdvancedLevel {

  private static final String NAME = "Portal Level";

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalSkillLevel_1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, NAME);
  }

  @Override
  protected void onFirstTick() {
    PortalFactory.createPortal(getPoint("portal"), Direction.LEFT, PortalColor.BLUE);
    Game.add(AntiMaterialBarrier.antiMaterialBarrier(getPoint("amg"), false));
    Game.add(
        AntiMaterialBarrier.antiMaterialBarrier(
            getPoint("amg").translate(Vector2.of(1, 0)), false));
    Game.add(
        AntiMaterialBarrier.antiMaterialBarrier(
            getPoint("amg").translate(Vector2.of(2, 0)), false));
  }

  @Override
  protected void onTick() {}
}
