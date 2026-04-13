package core.render;

import core.platform.RenderAdapter;
import core.camera.CameraViewportState;
import core.platform.litiengine.render.LitiengineLevelHideRenderSystem;
import core.camera.systems.CameraLifecycleSystem;
import contrib.debug.systems.LitiengineDebugDrawSystem;
import core.ui.StageHandle;
import core.utils.Point;
import java.util.List;
import java.util.Optional;

/**
 * LITIENGINE render adapter.
 *
 * <p>Bind client-side systems for the LITIENGINE backend.
 */
public final class LitiengineRenderAdapter implements RenderAdapter {
  @Override
  public List<SystemBinding> defaultRenderSystems() {
    return List.of(
      new SystemBinding(core.systems.SoundSystem.class, core.systems.SoundSystem::new),
      new SystemBinding(
        CameraLifecycleSystem.class, CameraLifecycleSystem::new),
      new SystemBinding(
        core.platform.litiengine.render.LitiengineSpriteRenderSystem.class,
        core.platform.litiengine.render.LitiengineSpriteRenderSystem::new),
      new SystemBinding(
        LitiengineLevelHideRenderSystem.class, LitiengineLevelHideRenderSystem::new));
  }

  @Override
  public Optional<Point> projectWorldToStage(Point worldPoint, StageHandle stageHandle) {
    if (worldPoint == null || stageHandle == null) {
      return Optional.empty();
    }

    return CameraViewportState.activeView()
      .map(view -> CameraViewportState.worldToScreen(worldPoint));
  }

  @Override
  public void toggleDebugHud() {
    LitiengineDebugDrawSystem.toggleHUD();
  }
}
