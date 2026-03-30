package core.ui;

import java.util.Optional;

/**
 * Engine-neutral fallback handle for UIs that currently have no concrete visual backend.
 *
 * <p>This is primarily used to keep dialog lifecycle and callback flows intact on backends
 * where no native dialog implementation exists yet.
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
