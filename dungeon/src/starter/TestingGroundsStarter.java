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
   * Setup and run the game.
   *
   * @param args The command line arguments.
   * @throws IOException If an error occurs while loading.
   */
  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);

    // Set up components and level
    DungeonLoader.addLevel(Tuple.of("playground", TestingGroundsLevel.class));

    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HudSystem());
    Game.add(new LevelTickSystem());

    DungeonLoader.loadLevel(0);

    Entity hero = HeroFactory.newHero();
    Game.add(hero);

    // start the game
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(true);
    Game.frameRate(30);
    Game.windowTitle("Basic Dungeon");
    Game.run();
  }
}
