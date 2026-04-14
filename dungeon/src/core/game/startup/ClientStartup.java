package core.game.startup;

import contrib.crafting.Crafting;
import core.Game;
import core.game.PreRunConfiguration;
import core.systems.LevelSystem;
import core.utils.logging.DungeonLogger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs the one-time client startup sequence after the runtime host has initialized the platform.
 *
 * <p>This step is intentionally separated from the concrete host runtime: host classes initialize
 * windowing, input, rendering, audio, and platform adapters; this class starts the actual dungeon
 * session by executing user setup, starting networking, loading recipes, and loading the initial
 * level.
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
      LOGGER.error("Client startup failed: {}", e.getMessage(), e);
      throw e;
    }
  }
}
