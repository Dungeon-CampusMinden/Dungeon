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

  /**
   * Advances the game simulation by one tick using the given delta time and executes systems that
   * are intended to run during rendering.
   *
   * <p>This is a convenience overload for {@link #tick(float, boolean)} with {@code renderSystems}
   * set to {@code true}.
   *
   * @param deltaSeconds elapsed time since the last frame/tick in seconds
   */
  public void tick(final float deltaSeconds) {
    tick(deltaSeconds, true);
  }

  /**
   * Advances the game simulation by one tick using the given delta time and executes systems based on
   * the {@code renderSystems} flag.
   *
   * <p>If {@code renderSystems} is {@code true}, systems that are intended to run during rendering will
   * be executed. If {@code false}, only non-rendering systems will be executed. This allows for
   * flexibility in controlling which systems run during different phases of the game loop.
   *
   * @param deltaSeconds elapsed time since the last frame/tick in seconds
   * @param renderSystems whether to execute systems that are intended to run during rendering
   */
  public void tick(final float deltaSeconds, final boolean renderSystems) {
    final boolean isMultiplayerClient =
      PreRunConfiguration.multiplayerEnabled() && !PreRunConfiguration.isNetworkServer();

    ECSManagement.executeOneTick(
      isMultiplayerClient ? System.AuthoritativeSide.CLIENT : System.AuthoritativeSide.BOTH,
      deltaSeconds,
      renderSystems);
  }
}
