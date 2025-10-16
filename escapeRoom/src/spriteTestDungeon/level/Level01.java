package spriteTestDungeon.level;

import contrib.components.*;
import core.level.DungeonLevel;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import hint.*;
import java.util.List;

/**
 * The Demolevel.
 *
 * <p>The player has to craft a Healpotion.
 */
public class Level01 extends DungeonLevel {
  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param customPoints The custom points of the level.
   */
  public Level01(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Demo");
  }

  @Override
  protected void onFirstTick() {}

  private void setupHints() {}

  @Override
  protected void onTick() {}
}
