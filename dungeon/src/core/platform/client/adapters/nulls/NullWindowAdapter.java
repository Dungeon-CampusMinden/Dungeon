package core.platform.client.adapters.nulls;

import core.platform.adapters.WindowAdapter;

/** Safe default window: no window, size 0, title no-op. */
public final class NullWindowAdapter implements WindowAdapter {
  @Override
  public int width() {
    return 0;
  }

  @Override
  public int height() {
    return 0;
  }

  @Override
  public void setTitle(String title) {
    // no-op
  }
}
