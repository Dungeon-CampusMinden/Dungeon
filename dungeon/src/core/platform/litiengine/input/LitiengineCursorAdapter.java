package core.platform.litiengine.input;

import core.Game;
import core.platform.CursorAdapter;
import core.platform.litiengine.render.LitiengineCameraViews;
import core.utils.Point;
import de.gurkenlabs.litiengine.input.Input;

import java.awt.geom.Point2D;

public final class LitiengineCursorAdapter implements CursorAdapter {

  @Override
  public int screenX() {
    Point2D p = Input.mouse().getLocation();
    return p == null ? 0 : (int) Math.round(p.getX());
  }

  @Override
  public int screenY() {
    Point2D p = Input.mouse().getLocation();
    return p == null ? 0 : (int) Math.round(p.getY());
  }

  @Override
  public Point world() {
    Point2D p = Input.mouse().getLocation();
    if (p == null) return new Point(0, 0);

    // Pull the same "camera view" offsets the renderer uses
    LitiengineCameraViews.View v = LitiengineCameraViews.get();

    final double sx = p.getX() - v.offsetX();
    final double sy = p.getY() - v.offsetY();

    final int tilePx = v.tilePx();
    final int levelHeight =
      Game.currentLevel()
        .map(l -> l.layout().length)
        .orElse(0);

    final float wx = (float) (sx / tilePx);
    final float wy = levelHeight > 0
      ? (float) ((levelHeight - 1) - (sy / tilePx))
      : (float) (-(sy / tilePx));

    return new Point(wx, wy);
  }
}
