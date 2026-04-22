package core.game.startup;

import core.Game;
import core.game.PreRunConfiguration;
import core.systems.LevelSystem;
import core.utils.IVoidFunction;
import core.utils.logging.DungeonLogger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles client-side initialization and setup during game startup.
 *
 * <p>This utility class manages the one-time initialization of the client, including
 * user setup, networking startup, and initial level loading. All initialization
 * is performed exactly once, even if the setup method is called multiple times.
 */
public final class ClientStartup {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ClientStartup.class);
  private static final AtomicBoolean DID_RUN = new AtomicBoolean(false);

  private ClientStartup() {}

  /**
   * Sets up the client and loads the initial level.
   *
   * <p>This method performs the following in order:
   * <ul>
   *   <li>1. Executes user setup (spawning hero and other initial entities)</li>
   *   <li>2. Starts networking (might be a LocalNetworkHandler for Single Player)</li>
   *   <li>3. Executes all configured client startup tasks</li>
   *   <li>4. Loads the initial level</li>
   * </ul>
   *
   * <p>Subsequent calls to this method will have no effect. If an exception occurs
   * during setup, it is logged and re-thrown.
   */
  public static void setupAndLoadInitialLevelOnce() {
    if (!DID_RUN.compareAndSet(false, true)) return;

    try {
      //User setup usually spawns the hero and other initial entities.
      PreRunConfiguration.userOnSetup().execute();

      // Start networking (even in single player this may be a LocalNetworkHandler).
      Game.network().start();

      for (IVoidFunction task : PreRunConfiguration.clientStartupTasks()) {
        task.execute();
      }

      // Load initial level once.
      Game.system(LevelSystem.class, LevelSystem::execute);
    } catch (Exception e) {
      LOGGER.error("Client startup failed: {}", e.getMessage(), e);
      throw e;
    }
  }
}
