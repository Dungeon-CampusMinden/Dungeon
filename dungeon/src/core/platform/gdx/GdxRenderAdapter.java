package core.platform.gdx;

import core.platform.Platform;
import core.platform.RenderAdapter;
import core.platform.gdx.render.DrawSystem;
import core.platform.gdx.render.GdxBlendUtils;
import core.platform.gdx.systems.GdxCameraSystem;
import core.systems.SoundSystem;
import java.util.List;

public final class GdxRenderAdapter implements RenderAdapter {

  @Override
  public List<SystemBinding> defaultRenderSystems() {
    return List.of(
      new SystemBinding(SoundSystem.class, SoundSystem::new),
      new SystemBinding(GdxCameraSystem.class, GdxCameraSystem::new),
      new SystemBinding(DrawSystem.class, DrawSystem::getInstance)
    );
  }

  @Override
  public void setPMABlending() {
    if (!Platform.runtime().supportsGdxRendering()) return;
    GdxBlendUtils.setPMABlending();
  }

  @Override
  public void setPMABlending(Object batch) {
    if (!Platform.runtime().supportsGdxRendering()) return;
    GdxBlendUtils.setPMABlending(batch);
  }

  @Override
  public void setStraightAlphaBlending() {
    if (!Platform.runtime().supportsGdxRendering()) return;
    GdxBlendUtils.setStraightAlphaBlending();
  }

  @Override
  public void setStraightAlphaBlending(Object batch) {
    if (!Platform.runtime().supportsGdxRendering()) return;
    GdxBlendUtils.setStraightAlphaBlending(batch);
  }
}
