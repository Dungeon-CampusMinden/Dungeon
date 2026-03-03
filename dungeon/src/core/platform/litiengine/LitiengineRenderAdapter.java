package core.platform.litiengine;

import core.platform.RenderAdapter;
import core.systems.SoundSystem;
import java.util.List;

/**
 * LITIENGINE render adapter.
 *
 * <p>At the current migration stage only bind client-side systems that are backend-agnostic
 * (e.g. SoundSystem). Rendering systems will be added incrementally.
 */
public final class LitiengineRenderAdapter implements RenderAdapter {
  @Override
  public List<SystemBinding> defaultRenderSystems() {
    return List.of(
      new SystemBinding(SoundSystem.class, SoundSystem::new)
    );
  }
}
