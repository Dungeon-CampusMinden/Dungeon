package core.platform.client;

import core.game.render.sprite.SpriteRenderSystem;
import core.platform.adapters.RenderAdapter;
import core.camera.CameraViewportState;
import core.camera.CameraLevelSyncSystem;
import core.ui.StageHandle;
import core.utils.Point;
import java.util.List;
import java.util.Optional;

/**
 * The {@code ClientRenderAdapter} class provides client-specific implementations for the
 * {@code RenderAdapter} interface. It defines rendering systems, world-to-stage coordinate
 * projection functionality.
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
        SpriteRenderSystem::new));
  }

  @Override
  public Optional<Point> projectWorldToStage(Point worldPoint, StageHandle stageHandle) {
    if (worldPoint == null || stageHandle == null) {
      return Optional.empty();
    }

    return CameraViewportState.activeViewport()
      .map(view -> CameraViewportState.worldToScreen(worldPoint));
  }
}
