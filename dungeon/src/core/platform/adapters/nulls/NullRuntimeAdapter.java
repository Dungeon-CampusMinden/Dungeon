package core.platform.adapters.nulls;

import core.platform.adapters.RuntimeAdapter;

/** Safe default runtime: no app lifecycle, considered headless. */
public final class NullRuntimeAdapter implements RuntimeAdapter {
  @Override
  public void requestExit() {
    // no-op by default (avoid killing tests or tools)
  }

  @Override
  public boolean isHeadless() {
    return true;
  }
}
