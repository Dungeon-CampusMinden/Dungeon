package contrib.client;

import contrib.debug.systems.DebugDrawSystem;
import contrib.modules.levelhide.LevelHideRenderSystem;
import core.Entity;
import core.platform.adapters.RenderAdapter;
import core.ui.StageHandle;
import core.utils.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adds contrib presentation systems and debug HUD behavior to an existing render adapter.
 *
 * <p>The core client render adapter stays independent of contrib systems; this wrapper is
 * installed by the contrib client bootstrap.
 */
public final class ContribRenderAdapter implements RenderAdapter {
  private final RenderAdapter delegate;

  /**
   * Creates a contrib render adapter wrapper.
   *
   * @param delegate render adapter to extend
   */
  public ContribRenderAdapter(RenderAdapter delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
  }

  @Override
  public List<SystemBinding> defaultRenderSystems() {
    List<SystemBinding> bindings = new ArrayList<>(delegate.defaultRenderSystems());
    bindings.add(new SystemBinding(LevelHideRenderSystem.class, LevelHideRenderSystem::new));
    return List.copyOf(bindings);
  }

  @Override
  public Optional<Point> projectWorldToStage(Point worldPoint, StageHandle stageHandle) {
    return delegate.projectWorldToStage(worldPoint, stageHandle);
  }

  @Override
  public void setPMABlending() {
    delegate.setPMABlending();
  }

  @Override
  public void setPMABlending(Object batch) {
    delegate.setPMABlending(batch);
  }

  @Override
  public void setStraightAlphaBlending() {
    delegate.setStraightAlphaBlending();
  }

  @Override
  public void setStraightAlphaBlending(Object batch) {
    delegate.setStraightAlphaBlending(batch);
  }

  @Override
  public void changeEntityDepth(Entity entity, int depth) {
    delegate.changeEntityDepth(entity, depth);
  }

  @Override
  public void toggleDebugHud() {
    DebugDrawSystem.toggleHUD();
  }
}
