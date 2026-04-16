package core.game;

import core.Game;
import core.System;
import core.utils.logging.DungeonLogger;

/**
 * Core implementation of the game loop tick and frame callback logic.
 *
 * <p>GameLoopCore handles the frame-by-frame execution of the game simulation, including network
 * message polling, system execution, and rendering. It acts as a bridge between the platform-specific
 * GameLoopHost and the ECS (Entity-Component-System) management.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Pre-render setup (network polling, frame callbacks)
 *   <li>Game simulation tick execution with system scheduling
 *   <li>Authoritative side determination (client/server/both)
 *   <li>Rendering system control
 * </ul>
 *
 * <p>This class is not instantiable directly by users; it is typically created and managed by
 * the GameLoopHost.
 */
public final class GameLoop {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GameLoop.class);

  /**
   * Performs pre-render setup for each frame.
   *
   * <p>This method executes before rendering and handles essential frame setup tasks:
   * <ul>
   *   <li>Polls and dispatches all pending network messages on the game thread
   *   <li>Executes user-defined frame callbacks for frame-level logic
   * </ul>
   *
   * <p>Network errors are logged as warnings and do not interrupt frame processing.
   *
   */
  public void beforeRender() {
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
