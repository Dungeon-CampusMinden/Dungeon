package core.platform.litiengine.levelEditor;

import core.utils.Point;

/** Shared snap behavior for LITIENGINE level editor modes. */
public enum EditorSnapMode {
  OnGrid("OnGrid"),
  QuarterGrid("QuarterGrid"),
  PixelGrid("PixelGrid"),
  OffGrid("OffGrid"),
  CheckerGridEven("CheckerGridEven"),
  CheckerGridOdd("CheckerGridOdd");

  private final String displayName;

  EditorSnapMode(String displayName) {
    this.displayName = displayName;
  }

  public String displayName() {
    return displayName;
  }

  public EditorSnapMode previousMode() {
    return values()[(this.ordinal() - 1 + values().length) % values().length];
  }

  public EditorSnapMode nextMode() {
    return values()[(this.ordinal() + 1) % values().length];
  }

  public Point getPosition(Point position) {
    return switch (this) {
      case OnGrid ->
        new Point((float) Math.floor(position.x()), (float) Math.floor(position.y()));
      case QuarterGrid ->
        new Point(
          (float) Math.floor(position.x() * 4) / 4.0f,
          (float) Math.floor(position.y() * 4) / 4.0f);
      case PixelGrid ->
        new Point(
          (float) Math.floor(position.x() * 16) / 16.0f,
          (float) Math.floor(position.y() * 16) / 16.0f);
      case CheckerGridEven, CheckerGridOdd -> {
        int parity = (this == CheckerGridEven) ? 0 : 1;

        float px = position.x() - 0.5f;
        float py = position.y() - 0.5f;

        float gx = (float) Math.floor(px);
        float gy = (float) Math.floor(py);

        float bestX = gx;
        float bestY = gy;
        float bestDist = Float.MAX_VALUE;

        for (int dx = 0; dx <= 1; dx++) {
          for (int dy = 0; dy <= 1; dy++) {
            float cx = gx + dx;
            float cy = gy + dy;
            if (((int) (cx + cy)) % 2 == parity) {
              float dist = (px - cx) * (px - cx) + (py - cy) * (py - cy);
              if (dist < bestDist) {
                bestDist = dist;
                bestX = cx;
                bestY = cy;
              }
            }
          }
        }

        yield new Point(bestX, bestY);
      }
      case OffGrid -> position;
    };
  }

  public boolean checkBlocked() {
    return this == OnGrid
      || this == QuarterGrid
      || this == CheckerGridEven
      || this == CheckerGridOdd;
  }
}
