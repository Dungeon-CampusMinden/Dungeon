package core.platform.adapters.nulls;

import core.platform.adapters.CursorAdapter;
import core.utils.Point;

/** Safe default: no cursor available. */
public final class NullCursorAdapter implements CursorAdapter {
  @Override
  public int screenX() {
    return 0;
  }

  @Override
  public int screenY() {
    return 0;
  }

  @Override
  public Point world() {
    return new Point(0, 0);
  }
}
