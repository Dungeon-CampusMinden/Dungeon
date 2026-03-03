package starter;

import contrib.entities.EntityFactory;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.GameLoop;
import core.game.GameLoopCore;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.systems.LevelSystem;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

/**
 * Dungeon bootstrap using the LITIENGINE host loop.
 *
 * <p>This mirrors BasicStarter, but runs the engine-agnostic loop core via the LITIENGINE host.
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

    // Use core-only onLevelLoad to avoid decoration/texture pipeline.
    Game.system(LevelSystem.class, ls -> ls.onLevelLoad(GameLoop.onLevelLoadCoreOnly));

    // Spawn hero even on LITIENGINE host.
    // Rendering is still disabled, but animations must not crash the game logic.
    Game.userOnSetup(() -> Game.add(EntityFactory.newHero()));

    // Initialize network/logging etc.
    Game.initialize();

    // Perform basic setup & load first level once so it doesn't spam in the loop.
    core.game.PreRunConfiguration.userOnSetup().execute();
    Game.network().start();
    contrib.crafting.Crafting.loadRecipes();
    Game.system(LevelSystem.class, LevelSystem::execute);

    // Start LITIENGINE host loop (ticks GameLoopCore).
    core.game.litiengine.LitiengineGameLoopHost.run(args, new GameLoopCore());
  }
}
