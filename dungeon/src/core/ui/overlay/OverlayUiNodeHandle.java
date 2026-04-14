package core.ui.overlay;

import core.ui.StageHandle;
import core.ui.UiNodeHandle;
import java.util.Optional;


/**
 * A UI node handle adapter for overlay-based UI elements.
 *
 * <p>OverlayUiNodeHandle implements UiNodeHandle to provide a unified interface for managing
 * UiOverlay instances within the UI system. It acts as a wrapper that delegates operations
 * (visibility, positioning, z-ordering) to the underlying overlay and its registry.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Managing overlay attachment and removal from the registry
 *   <li>Controlling overlay visibility and positioning
 *   <li>Handling z-ordering for overlay layering
 *   <li>Centering overlays on stage handles
 *   <li>Providing type-safe unwrapping of the underlying overlay
 * </ul>
 *
 * <p>This handle integrates overlays with the standard UI node management system, allowing
 * overlays to be treated as regular UI components.
 */
public final class OverlayUiNodeHandle implements UiNodeHandle {

  private final UiOverlay overlay;

  /**
   * Constructs an OverlayUiNodeHandle for the given overlay.
   *
   * @param overlay the UiOverlay to manage (must not be null)
   */
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
