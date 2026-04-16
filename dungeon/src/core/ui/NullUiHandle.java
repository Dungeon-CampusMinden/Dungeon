package core.ui;

import java.util.Optional;

/**
 * A no-operation implementation of the {@code UiHandle} interface.
 *
 * <p>The {@code NullUiHandle} class provides a non-functional or "null object" implementation
 * of {@code UiHandle}. This implementation serves as a placeholder and performs no meaningful
 * actions. It is useful for scenarios where a non-null implementation of {@code UiHandle} is
 * required but no actual behavior is desired.
 *
 * <p>Key characteristics:
 * <ul>
 *   <li>Visibility state can be managed but has no effect on the UI.</li>
 *   <li>Operations such as attaching to a stage, moving to the front, and centering are no-ops.</li>
 *   <li>Always returns fixed values for methods such as {@code isAttached()} and {@code getZIndex()}.</li>
 *   <li>Unwrap operations always return {@code Optional.empty()}.</li>
 * </ul>
 */
public final class NullUiHandle implements UiHandle {
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
