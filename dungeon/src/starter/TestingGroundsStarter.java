package starter;

import contrib.entities.HeroFactory;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.configuration.KeyboardConfig;
import core.level.TestingGroundsLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;

/** Run this class to start the TestingGroundsLevel of the Dungeon. */
public class TestingGroundsStarter {

  /**
   * WTF? .
   *
   * @param args foo
   * @throws IOException foo
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    DungeonLoader.addLevel(Tuple.of("playground", TestingGroundsLevel.class));

    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HudSystem());
    Game.add(new LevelTickSystem());

    Entity hero = HeroFactory.newHero();
    Game.add(hero);
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    DungeonLoader.loadLevel(0);

    Game.disableAudio(true);
    Game.frameRate(30);
    Game.windowTitle("Basic Dungeon");
    Game.run();
  }
}
