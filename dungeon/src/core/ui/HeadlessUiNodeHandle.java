package core.ui;

import java.util.Optional;

/**
 * A no-op implementation of UiNodeHandle for headless or testing scenarios.
 *
 * <p>HeadlessUiNodeHandle provides a minimal, non-functional implementation of the UiNodeHandle
 * interface. It is useful for headless environments, unit tests, or scenarios where UI node
 * management is not needed but the interface compliance is required.
 *
 * <p>Behavior characteristics:
 * <ul>
 *   <li>Only maintains a visibility flag (initially true)
 *   <li>All attachment/positioning/z-ordering operations are no-ops
 *   <li>Always reports as not attached
 *   <li>Unwrap operations always return empty Optional
 *   <li>Z-index always returns 0
 * </ul>
 *
 * <p>This implementation is suitable for environments where UI rendering is not performed
 * and UI management is not necessary.
 */
public final class HeadlessUiNodeHandle implements UiNodeHandle {
  private boolean visible = true;

  @Override
  public void remove() {
    visible = false;
  }

  @Override
  public int getZIndex() {
    return 0;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  @Override
  public boolean isAttached() {
    return false;
  }

  @Override
  public void attachTo(StageHandle stageHandle) {
    // no-op
  }

  @Override
  public void toFront() {
    // no-op
  }

  @Override
  public void centerOn(StageHandle stageHandle) {
    // no-op
  }

  @Override
  public <T> Optional<T> unwrap(Class<T> type) {
    return Optional.empty();
  }
}
