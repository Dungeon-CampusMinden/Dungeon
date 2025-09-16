package starter;

import contrib.entities.HeroFactory;
import contrib.systems.AISystem;
import contrib.systems.CollisionSystem;
import contrib.systems.EventScheduler;
import contrib.systems.FallingSystem;
import contrib.systems.HealthBarSystem;
import contrib.systems.HealthSystem;
import contrib.systems.HudSystem;
import contrib.systems.IdleSoundSystem;
import contrib.systems.LevelEditorSystem;
import contrib.systems.LevelTickSystem;
import contrib.systems.LeverSystem;
import contrib.systems.ManaBarSystem;
import contrib.systems.ManaRestoreSystem;
import contrib.systems.PathSystem;
import contrib.systems.PitSystem;
import contrib.systems.PressurePlateSystem;
import contrib.systems.ProjectileSystem;
import contrib.systems.SpikeSystem;
import core.Game;
import core.configuration.KeyboardConfig;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;

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

  /**
   * Main entry point to launch the basic dungeon game.
   *
   * @param args command-line arguments (not used in this starter)
   */
  public static void main(String[] args) {
    Game.initBaseLogger(Level.WARNING);
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.disableAudio(true);
    Game.userOnSetup(
        () -> {
          try {
            Game.add(HeroFactory.newHero());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    setupSystems();
    Game.frameRate(30);
    Game.windowTitle("Basic Dungeon");
    Game.run();
  }

  private static void setupSystems() {
    Game.add(new ProjectileSystem()); // handles shooting or thrown objects
    Game.add(new HudSystem()); // displays score, health, and other info on screen
    Game.add(new HealthSystem()); // manages character health
    Game.add(new HealthBarSystem()); // shows health bar on HUD
    Game.add(new IdleSoundSystem()); // plays sounds when characters are idle
    Game.add(new PressurePlateSystem()); // handles pressure plates in the level
    Game.add(new LeverSystem()); // handles levers for puzzles
    Game.add(new CollisionSystem()); // detects collisions between objects
    Game.add(new FallingSystem()); // handles falling objects or traps
    Game.add(new PitSystem()); // handles pits that characters can fall into
    Game.add(new EventScheduler()); // schedules timed events
    Game.add(new SpikeSystem()); // handles spikes damaging the hero
    Game.add(new LevelTickSystem()); // executes recurring level events
    Game.add(new AISystem()); // handles enemy AI behavior
    Game.add(new PathSystem()); // handles pathfinding for AI
    Game.add(new ManaBarSystem());
    Game.add(new ManaRestoreSystem());

  }

}
