package core.platform.gdx;

import core.platform.CameraAdapter;
import core.platform.gdx.systems.GdxCameraSystem;

/** GDX-backed camera adapter delegating to {@link GdxCameraSystem}. */
public final class GdxCameraAdapter implements CameraAdapter {

  @Override
  public boolean supportsZoom() {
    return true;
  }

  @Override
  public float zoom() {
    return GdxCameraSystem.camera().zoom;
  }

  @Override
  public void zoom(float zoom) {
    GdxCameraSystem.camera().zoom = zoom;
  }
}
