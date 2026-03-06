package core.game.bootstrap;

import contrib.crafting.Crafting;
import core.Game;
import core.game.PreRunConfiguration;
import core.systems.LevelSystem;
import core.utils.logging.DungeonLogger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs the one-time "client startup" sequence:
 * <ul>
 *  <li> userOnSetup()
 *  <li> start network handler
 *  <li> load recipes
 *  <li> execute LevelSystem once to load initial level
 * </ul>
 * This is intentionally engine-agnostic (no gdx/litiengine imports).
 */
public final class ClientStartup {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ClientStartup.class);
  private static final AtomicBoolean DID_RUN = new AtomicBoolean(false);

  private ClientStartup() {}

  public static void setupAndLoadInitialLevelOnce() {
    if (!DID_RUN.compareAndSet(false, true)) return;

    try {
      //User setup usually spawns the hero and other initial entities.
      PreRunConfiguration.userOnSetup().execute();

      // Start networking (even in single player this may be a LocalNetworkHandler).
      Game.network().start();

      // Load game data.
      Crafting.loadRecipes();

      // Load initial level once.
      Game.system(LevelSystem.class, LevelSystem::execute);
    } catch (Exception e) {
      LOGGER.error("ClientStartup failed: {}", e.getMessage(), e);
      throw e;
    }
  }
}
