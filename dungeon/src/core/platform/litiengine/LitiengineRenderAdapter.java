package core.platform.litiengine;

import core.platform.RenderAdapter;
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
      new SystemBinding(core.systems.SoundSystem.class, core.systems.SoundSystem::new),
      new SystemBinding(core.platform.litiengine.render.LitiengineDebugDrawSystem.class,
        core.platform.litiengine.render.LitiengineDebugDrawSystem::new)
    );
  }
}
