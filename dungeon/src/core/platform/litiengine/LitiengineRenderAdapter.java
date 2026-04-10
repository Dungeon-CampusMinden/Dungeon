package core.platform.litiengine;

import core.platform.RenderAdapter;
import core.platform.litiengine.render.LitiengineCameraViews;
import core.platform.litiengine.systems.LitiengineCameraLifecycleSystem;
import core.platform.litiengine.systems.LitiengineDebugDrawSystem;
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
        LitiengineCameraLifecycleSystem.class, LitiengineCameraLifecycleSystem::new),
      new SystemBinding(
        core.platform.litiengine.render.LitiengineSpriteRenderSystem.class,
        core.platform.litiengine.render.LitiengineSpriteRenderSystem::new));
  }

  @Override
  public Optional<Point> projectWorldToStage(Point worldPoint, StageHandle stageHandle) {
    if (worldPoint == null || stageHandle == null) {
      return Optional.empty();
    }

    return LitiengineCameraViews.activeView().map(view -> LitiengineCameraViews.worldToScreen(worldPoint));
  }

  @Override
  public void toggleDebugHud() {
    LitiengineDebugDrawSystem.toggleHUD();
  }
}
