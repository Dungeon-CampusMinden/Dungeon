package core.platform.render;

import core.game.render.sprite.SpriteRenderSystem;
import core.platform.RenderAdapter;
import core.camera.CameraViewportState;
import contrib.modules.levelHide.LevelHideRenderSystem;
import core.camera.systems.CameraLifecycleSystem;
import contrib.debug.systems.DebugDrawSystem;
import core.ui.StageHandle;
import core.utils.Point;
import java.util.List;
import java.util.Optional;

/**
 * LITIENGINE render adapter.
 *
 * <p>Bind client-side systems for the LITIENGINE backend.
 */
public final class ClientRenderAdapter implements RenderAdapter {
  @Override
  public List<SystemBinding> defaultRenderSystems() {
    return List.of(
      new SystemBinding(core.systems.SoundSystem.class, core.systems.SoundSystem::new),
      new SystemBinding(
        CameraLifecycleSystem.class, CameraLifecycleSystem::new),
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
