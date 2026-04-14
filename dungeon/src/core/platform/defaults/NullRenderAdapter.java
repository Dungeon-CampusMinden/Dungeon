package core.platform.defaults;

import core.platform.RenderAdapter;
import java.util.List;

/**
 * A safe default implementation of the {@link RenderAdapter} interface that disables rendering functionality.
 * This implementation provides no-op behavior for rendering systems, ensuring compatibility with
 * headless or test environments.
 *
 * <p>The {@code NullRenderAdapter} class returns an empty list for the default render systems,
 * indicating that no rendering systems are registered by default.
 */
public final class NullRenderAdapter implements RenderAdapter {
  @Override
  public List<SystemBinding> defaultRenderSystems() {
    return List.of();
  }
}
