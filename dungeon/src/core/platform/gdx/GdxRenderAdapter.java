package core.platform.gdx;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import core.platform.Platform;
import core.platform.RenderAdapter;
import core.platform.gdx.render.GdxBlendUtils;
import core.platform.gdx.systems.GdxCameraSystem;
import core.systems.SoundSystem;
import core.ui.StageHandle;
import core.utils.Point;
import java.util.List;
import java.util.Optional;

public final class GdxRenderAdapter implements RenderAdapter {

  @Override
  public List<SystemBinding> defaultRenderSystems() {
    return List.of(
      new SystemBinding(SoundSystem.class, SoundSystem::new));
  }

  @Override
  public Optional<Point> projectWorldToStage(Point worldPoint, StageHandle stageHandle) {
    if (!Platform.runtime().supportsGdxRendering()) return Optional.empty();
    if (worldPoint == null || stageHandle == null) return Optional.empty();

    Stage stage = stageHandle.unwrap(Stage.class).orElse(null);
    if (stage == null) return Optional.empty();

    Vector3 screenCoords =
      GdxCameraSystem.camera().project(new Vector3((float) worldPoint.x(), (float) worldPoint.y(), 0));

    float stageX =
      screenCoords.x / stage.getViewport().getScreenWidth() * stage.getWidth();
    float stageY =
      screenCoords.y / stage.getViewport().getScreenHeight() * stage.getHeight();

    return Optional.of(new Point(stageX, stageY));
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
