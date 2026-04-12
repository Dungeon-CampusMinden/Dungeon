package core.ui.overlay;

import core.ui.StageHandle;
import core.ui.UiNodeHandle;
import java.util.Optional;

/** LITIENGINE-backed {@link UiNodeHandle} for custom screen overlays. */
public final class LitiengineUiNodeHandle implements UiNodeHandle {

  private final LitiengineUiOverlay overlay;

  public LitiengineUiNodeHandle(LitiengineUiOverlay overlay) {
    this.overlay = overlay;
  }

  @Override
  public void remove() {
    overlay.visible(false);
    LitiengineUiOverlayRegistry.remove(overlay);
  }

  @Override
  public int getZIndex() {
    return 0;
  }

  @Override
  public boolean isVisible() {
    return overlay.visible();
  }

  @Override
  public void setVisible(boolean visible) {
    overlay.visible(visible);
  }

  @Override
  public boolean isAttached() {
    return LitiengineUiOverlayRegistry.contains(overlay);
  }

  @Override
  public void attachTo(StageHandle stageHandle) {
    overlay.visible(true);
    LitiengineUiOverlayRegistry.add(overlay);
  }

  @Override
  public void toFront() {
    LitiengineUiOverlayRegistry.toFront(overlay);
  }

  @Override
  public void centerOn(StageHandle stageHandle) {
    if (stageHandle == null) {
      return;
    }

    int x = Math.round((stageHandle.getWidth() - overlay.width()) / 2f);
    int y = Math.round((stageHandle.getHeight() - overlay.height()) / 2f);

    overlay.x(x);
    overlay.y(y);
  }

  @Override
  public <T> Optional<T> unwrap(Class<T> type) {
    if (type.isInstance(overlay)) {
      return Optional.of(type.cast(overlay));
    }
    return Optional.empty();
  }
}
