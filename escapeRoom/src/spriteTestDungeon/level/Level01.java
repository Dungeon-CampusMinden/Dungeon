package spriteTestDungeon.level;

import contrib.components.*;
import contrib.entities.deco.CompositeDecoFactory;
import core.Game;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import hint.*;
import java.util.Map;

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
   * @param namedPoints The custom points of the level.
   */
  public Level01(LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Demo");
  }

  @Override
  protected void onFirstTick() {
    CompositeDecoFactory.createTreeChain(new Point(10, 20), 10).forEach(Game::add);
    CompositeDecoFactory.createTreeChain(new Point(8.5f, 22), 12).forEach(Game::add);
    CompositeDecoFactory.createTreeChain(new Point(10, 24), 10).forEach(Game::add);
  }

  private void setupHints() {}

  @Override
  protected void onTick() {}
}
