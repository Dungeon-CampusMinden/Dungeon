package core.game;

import core.Game;
import core.System;
import core.systems.CameraSystem;
import core.systems.DrawSystem;
import core.utils.InputManager;
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

  /** Called once per frame BEFORE clearing the screen (host-specific). */
  public void beforeRender(final float deltaSeconds) {
    // Keep DrawSystem projection in sync with current camera before rendering systems.
    ECSManagement.system(
      DrawSystem.class,
      drawSystem -> DrawSystem.batch().setProjectionMatrix(CameraSystem.camera().combined));

    // Drain inbound network messages on the game thread before running systems.
    try {
      Game.network().pollAndDispatch();
    } catch (Exception e) {
      LOGGER.warn("Error while polling network messages: {}", e.getMessage(), e);
    }

    // Frame callbacks and sound update are engine-agnostic.
    Game.soundPlayer().update(deltaSeconds);
    PreRunConfiguration.userOnFrame().execute();
  }

  /** Called once per frame AFTER clearing the screen (host-specific). */
  public void tick(final float deltaSeconds) {
    final boolean isMultiplayerClient =
      PreRunConfiguration.multiplayerEnabled() && !PreRunConfiguration.isNetworkServer();

    ECSManagement.executeOneTick(
      isMultiplayerClient ? System.AuthoritativeSide.CLIENT : System.AuthoritativeSide.BOTH,
      deltaSeconds);

    // Input and camera post-update (same order as before).
    InputManager.update();
    CameraSystem.camera().update();
  }
}
