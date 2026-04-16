package core.ui.overlay;

import core.ui.StageHandle;
import core.ui.UiHandle;
import java.util.Optional;

/**
 * A handle for managing the lifecycle, visibility, and positioning of a {@link UiOverlay}.
 *
 * <p>The {@code OverlayHandle} class provides an implementation of the {@link UiHandle} interface
 * for handling {@link UiOverlay} instances.
 *
 * <p>It allows controlling overlay properties such as visibility,
 * attachment to stages, z-order, and positioning, and also facilitates type-safe access to
 * the underlying {@link UiOverlay} object.
 *
 * <p>Key functionalities:
 * <ul>
 *   <li>Attaching/detaching the overlay to/from a stage.</li>
 *   <li>Controlling the overlay's visibility and z-order.</li>
 *   <li>Centering the overlay on a stage.</li>
 *   <li>Providing type-safe unwrapping of the underlying overlay instance.</li>
 * </ul>
 */
public final class OverlayHandle implements UiHandle {

  private final UiOverlay overlay;

  /**
   * Constructs a new {@code OverlayHandle} instance for managing the given overlay.
   *
   * @param overlay the {@link UiOverlay} instance to be managed
   */
  public OverlayHandle(UiOverlay overlay) {
    this.overlay = overlay;
  }

  @Override
  public void remove() {
    overlay.visible(false);
    OverlayManager.remove(overlay);
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
    return OverlayManager.contains(overlay);
  }

  @Override
  public void attachTo(StageHandle stageHandle) {
    overlay.visible(true);
    OverlayManager.add(overlay);
  }

  @Override
  public void toFront() {
    OverlayManager.toFront(overlay);
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
