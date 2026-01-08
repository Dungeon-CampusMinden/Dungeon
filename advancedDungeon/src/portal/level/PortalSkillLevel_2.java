package portal.level;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import portal.util.AdvancedLevel;

import java.util.Map;
public class PortalSkillLevel_2 extends AdvancedLevel {

  private static final String NAME = "Portal Level";

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
  }
  @Override
  protected void onTick() {}
}

