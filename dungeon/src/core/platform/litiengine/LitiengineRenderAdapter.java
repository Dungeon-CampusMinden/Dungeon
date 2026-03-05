package core.platform.litiengine;

import core.platform.RenderAdapter;
import java.util.List;

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
        core.platform.litiengine.render.LitiengineSpriteRenderSystem.class,
        core.platform.litiengine.render.LitiengineSpriteRenderSystem::new)
    );
  }
}
