package core.platform;

import core.utils.Point;

/** Provides cursor position (screen + world) for the active backend. */
public interface CursorAdapter {
  /** Screen X in pixels (origin: top-left). */
  int screenX();

  /** Screen Y in pixels (origin: top-left). */
  int screenY();

  /** Cursor position in game world coordinates (tile/world units). */
  Point world();
}
