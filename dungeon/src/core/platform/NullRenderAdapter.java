package core.platform;

import java.util.List;

public final class NullRenderAdapter implements RenderAdapter {
  @Override
  public List<SystemBinding> defaultRenderSystems() {
    return List.of();
  }
}
