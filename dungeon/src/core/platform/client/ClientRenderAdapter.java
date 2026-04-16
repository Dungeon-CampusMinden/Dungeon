package core.platform.client;

import core.game.render.sprite.SpriteRenderSystem;
import core.platform.RenderAdapter;
import core.camera.CameraViewportState;
import contrib.modules.levelHide.LevelHideRenderSystem;
import core.camera.systems.CameraLevelSyncSystem;
import contrib.debug.systems.DebugDrawSystem;
import core.ui.StageHandle;
import core.utils.Point;
import java.util.List;
import java.util.Optional;

/**
 * The {@code ClientRenderAdapter} class provides client-specific implementations for the
 * {@code RenderAdapter} interface. It defines rendering systems, world-to-stage coordinate
 * projection, and debug HUD toggling functionalities.
 */
public final class ClientRenderAdapter implements RenderAdapter {
  @Override
  public List<SystemBinding> defaultRenderSystems() {
    return List.of(
      new SystemBinding(core.systems.SoundSystem.class, core.systems.SoundSystem::new),
      new SystemBinding(
        CameraLevelSyncSystem.class, CameraLevelSyncSystem::new),
      new SystemBinding(
        SpriteRenderSystem.class,
        SpriteRenderSystem::new),
      new SystemBinding(
        LevelHideRenderSystem.class, LevelHideRenderSystem::new));
  }

  @Override
  public Optional<Point> projectWorldToStage(Point worldPoint, StageHandle stageHandle) {
    if (worldPoint == null || stageHandle == null) {
      return Optional.empty();
    }

    return CameraViewportState.activeViewport()
      .map(view -> CameraViewportState.worldToScreen(worldPoint));
  }

  @Override
  public void toggleDebugHud() {
    DebugDrawSystem.toggleHUD();
  }
}
