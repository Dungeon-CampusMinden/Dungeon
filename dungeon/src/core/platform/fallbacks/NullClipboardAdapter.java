package core.platform.fallbacks;

import core.platform.adapters.ClipboardAdapter;

/** Safe default clipboard: unsupported, write operations are ignored. */
public final class NullClipboardAdapter implements ClipboardAdapter {
  @Override
  public void setContents(String text) {
    // no-op
  }

  @Override
  public boolean isSupported() {
    return false;
  }
}
