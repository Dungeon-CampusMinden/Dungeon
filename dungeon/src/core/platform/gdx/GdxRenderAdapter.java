package core.platform.gdx;

import core.platform.RenderAdapter;
import core.systems.DrawSystem;
import core.systems.SoundSystem;
import java.util.List;

public final class GdxRenderAdapter implements RenderAdapter {

  @Override
  public List<SystemBinding> defaultRenderSystems() {
    return List.of(
      new SystemBinding(SoundSystem.class, SoundSystem::new),
      new SystemBinding(DrawSystem.class, DrawSystem::getInstance)
    );
  }
}
