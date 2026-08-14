package rooms.demo;

import engine.Game;
import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import feature.entities.deco.CompositeDecoFactory;
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
