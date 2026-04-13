package core.ui.overlay;

import core.ui.StageHandle;

import java.util.Optional;

/** LITIENGINE-backed {@link core.ui.UiNodeHandle} for custom screen overlays. */
public final class OverlayUiNodeHandle implements core.ui.UiNodeHandle {

  private final UiOverlay overlay;

  public OverlayUiNodeHandle(UiOverlay overlay) {
    this.overlay = overlay;
  }

  @Override
  public void remove() {
    overlay.visible(false);
    UiOverlayRegistry.remove(overlay);
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
    return UiOverlayRegistry.contains(overlay);
  }

  @Override
  public void attachTo(StageHandle stageHandle) {
    overlay.visible(true);
    UiOverlayRegistry.add(overlay);
  }

  @Override
  public void toFront() {
    UiOverlayRegistry.toFront(overlay);
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
