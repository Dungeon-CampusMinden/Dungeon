package starter;

import contrib.entities.HeroFactory;
import contrib.utils.components.Debugger;
import core.Game;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.systems.SoundSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogLevel;
import core.utils.logging.DungeonLogger;
import core.utils.logging.DungeonLoggerConfig;
import java.io.IOException;

/**
 * Entry point for running a minimal dungeon game instance.
 *
 * <p>This starter initializes the game framework, loads the dungeon configuration, spawns a basic
 * hero, and starts the game loop. It is mainly used to verify that the engine runs correctly with a
 * simple setup.
 *
 * <p>Usage: run with the Gradle task {@code runBasicStarter}.
 */
public class BasicStarter {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(BasicStarter.class);

  /**
   * Main entry point to launch the basic dungeon game.
   *
   * @param args command-line arguments (not used in this starter)
   */
  public static void main(String[] args) {
    DungeonLoggerConfig.initWithLevel(DungeonLogLevel.INFO);
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.userOnSetup(
        () -> {
          try {
            onSetup();
            Game.add(HeroFactory.newHero());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    Game.frameRate(30);
    Game.windowTitle("Basic Dungeon");
    Game.run();
  }

  private static void onSetup() throws IOException {
    Game.add(new Debugger());
    Game.add(new SoundSystem());
  }
}
