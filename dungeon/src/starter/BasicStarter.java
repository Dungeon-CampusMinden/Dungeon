package starter;

import contrib.entities.HeroFactory;
import contrib.systems.ProjectileSystem;
import core.Game;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @return WTF? .
 */
public class BasicStarter implements IStarter{

  /**
   * WTF? .
   *
   * @param args foo
   * @throws IOException foo
   */
  public static void main(String[] args) throws IOException {
    Game.run(new BasicStarter());
  }

  @Override
  public void start() {
    Game.initBaseLogger(Level.WARNING);
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.frameRate(30);
    Game.add(new ProjectileSystem());
    Game.windowTitle("Basic Dungeon");
    try {
      Game.add(HeroFactory.newHero());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
