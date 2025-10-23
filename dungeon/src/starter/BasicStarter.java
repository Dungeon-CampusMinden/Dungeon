package starter;

import com.badlogic.gdx.Gdx;
import contrib.entities.HeroFactory;
import contrib.systems.EventScheduler;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.SoundComponent;
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
    Game.add(new EventScheduler());
    Game.add(
        new System() {
          @Override
          public void execute() {
            // on 'k' key
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F1)) {
              LOGGER.info("Spawning fireball sound entity");
              // Create an entity for a fireball sound effect
              Entity fireballEntity = new Entity();

              // Add SoundComponent: soundId, baseVolume, looping, maxDistance, attenuationFactor
              SoundComponent soundComponent =
                  new SoundComponent(
                      "fireball",
                      0.5f,
                      true,
                      20f,
                      0.1f,
                      () -> {
                        Game.remove(fireballEntity);
                      });
              fireballEntity.add(soundComponent);

              // Add PositionComponent to place it in the world
              fireballEntity.add(new PositionComponent(1, 1));
              fireballEntity.add(new DrawComponent(new SimpleIPath("skills/fireball")));

              // Add the entity to the game
              Game.add(fireballEntity);
            }
          }
        });
  }
}
