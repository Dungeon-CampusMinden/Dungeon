package starter;

import contrib.entities.EntityFactory;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.GameLoop;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

/**
 * Entry point for the LITIENGINE-based dungeon game.
 *
 * <p>This class initializes and starts the game by configuring levels, loading settings,
 * setting up the game window, and launching the LITIENGINE host loop.
 *
 * <p>This class is not instantiable.
 */
public final class LitiengineStarter {
  private LitiengineStarter() {}

  static void main(String[] args) {
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));

    try {
      Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Game.disableAudio(false);
    Game.frameRate(30);
    Game.windowTitle("LITIENGINE Dungeon");

    // Spawn hero
    Game.userOnSetup(
      () -> {
        Game.add(EntityFactory.newHero());
        LitienginePlatformBootstrap.installHudSystems();
        LitienginePlatformBootstrap.installDebugger();
      });

    // Install the concrete loop host explicitly instead of bypassing GameLoop.
    LitienginePlatformBootstrap.init();

    // Initialize network/logging etc.
    Game.initialize();

    // Start through the host-agnostic facade.
    GameLoop.run(args);
  }
}
