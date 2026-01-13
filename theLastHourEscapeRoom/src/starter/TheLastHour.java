package starter;

import contrib.entities.EntityFactory;
import contrib.modules.keypad.KeypadSystem;
import contrib.systems.CollisionSystem;
import contrib.systems.DebugDrawSystem;
import contrib.systems.HudSystem;
import contrib.systems.LevelEditorSystem;
import contrib.utils.components.Debugger;
import core.Game;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLoggerConfig;
import level.LastHourLevel1;

import java.io.IOException;

/**
 * Entry point for running a minimal dungeon game instance.
 *
 * <p>This starter initializes the game framework, loads the dungeon configuration, spawns a basic
 * player, and starts the game loop. It is mainly used to verify that the engine runs correctly with
 * a simple setup.
 *
 * <p>Usage: run with the Gradle task {@code runBasicStarter}.
 */
public class TheLastHour {

  /**
   * Main entry point to launch the basic dungeon game.
   *
   * @param args command-line arguments (not used in this starter)
   */
  public static void main(String[] args) {
    DungeonLoader.addLevel(Tuple.of("lasthour", LastHourLevel1.class));
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.disableAudio(true);
    Game.userOnSetup(TheLastHour::onUserSetup);
    Game.frameRate(60);
    Game.windowTitle("The Last Hour");
    Game.run();
  }

  public static void onUserSetup(){
    Game.add(EntityFactory.newHero());

    Game.add(new CollisionSystem());
    Game.add(new KeypadSystem());
    Game.add(new Debugger());
    Game.add(new DebugDrawSystem());
    Game.add(new LevelEditorSystem());
  }
}
