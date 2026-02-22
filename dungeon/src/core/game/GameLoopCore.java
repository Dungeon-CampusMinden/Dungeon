package core.game;

import core.Game;
import core.System;
import core.utils.logging.DungeonLogger;

/**
 * Engine-agnostic core loop logic.
 *
 * <p>This class contains the per-frame logic that should not depend on a concrete rendering/input
 * backend. Concrete engines (libGDX, LITIENGINE, ...) are expected to call these methods from
 * their host loop and provide delta time.
 */
public final class GameLoopCore {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GameLoopCore.class);

  /** Called once per frame BEFORE host-specific rendering (e.g., clearing the screen). */
  public void beforeRender(final float deltaSeconds) {
    // Drain inbound network messages on the game thread before running systems.
    try {
      Game.network().pollAndDispatch();
    } catch (Exception e) {
      LOGGER.warn("Error while polling network messages: {}", e.getMessage(), e);
    }

    // Frame callbacks are backend-agnostic.
    PreRunConfiguration.userOnFrame().execute();
  }

  /** Called once per frame to execute one ECS tick. */
  public void tick(final float deltaSeconds) {
    final boolean isMultiplayerClient =
      PreRunConfiguration.multiplayerEnabled() && !PreRunConfiguration.isNetworkServer();

    ECSManagement.executeOneTick(
      isMultiplayerClient ? System.AuthoritativeSide.CLIENT : System.AuthoritativeSide.BOTH,
      deltaSeconds);
  }
}
