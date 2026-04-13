package core.ui.overlay;

import java.awt.Graphics2D;

/**
 * Minimal overlay abstraction for custom LITIENGINE HUD/dialog rendering.
 *
 * <p>Coordinates are window/screen coordinates with origin in the top-left corner.
 */
public interface UiOverlay {

  void render(Graphics2D g);

  int x();

  void x(int x);

  int y();

  void y(int y);

  int width();

  void width(int width);

  int height();

  void height(int height);

  boolean visible();

  void visible(boolean visible);

  default boolean contains(int px, int py) {
    return px >= x() && px <= x() + width() && py >= y() && py <= y() + height();
  }
}
