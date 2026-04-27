package contrib.client.render;

import contrib.debug.draw.DebugDrawService;
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
 * A wrapper implementation of the {@link RenderAdapter} interface that extends or modifies
 * the behavior of an existing RenderAdapter instance.
 *
 * <p>The {@code LayeredRenderAdapter} delegates rendering-related operations to an underlying
 * {@code RenderAdapter}, allowing additional functionality to be layered on top of the base implementation.
 *
 * <p>This class is particularly useful when you want to enhance or override specific behaviors
 * without modifying the original {@code RenderAdapter}.
 */
public final class LayeredRenderAdapter implements RenderAdapter {
  private final RenderAdapter delegate;

  /**
   * Wraps an existing {@link RenderAdapter} instance to extend or modify its functionality.
   *
   * <p>This adapter delegates all method calls to the provided {@code delegate} RenderAdapter, allowing
   * additional behavior to be layered on top of the base implementation without modifying it directly.
   *
   * @param delegate the underlying {@link RenderAdapter} to which calls are delegated; must not be null
   */
  public LayeredRenderAdapter(RenderAdapter delegate) {
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
    DebugDrawService.toggleHUD();
  }
}
