package core.platform.litiengine;

import core.platform.CameraAdapter;
import core.platform.litiengine.render.LitiengineCameraState;

/** Camera adapter for the LITIENGINE backend. */
public final class LitiengineCameraAdapter implements CameraAdapter {

  @Override
  public boolean supportsZoom() {
    return true;
  }

  @Override
  public float zoom() {
    return LitiengineCameraState.zoom();
  }

  @Override
  public void zoom(float zoom) {
    LitiengineCameraState.zoom(zoom);
  }
}
