package feature.prefabs;

import com.badlogic.gdx.graphics.Color;
import engine.utils.Point;

/**
 * Rendering abstraction used by prefabs to describe editor feedback without depending on editor
 * implementation classes.
 */
public interface PrefabEditorFeedback {

  /**
   * Draws a point marker.
   *
   * @param point marker position
   * @param label marker label
   */
  void point(Point point, String label);

  /**
   * Draws a line, optionally with an arrow head.
   *
   * @param from line start
   * @param to line end
   * @param arrow whether to draw an arrow head
   */
  void line(Point from, Point to, boolean arrow);

  /**
   * Draws a line with an optional color override.
   *
   * <p>A {@code null} color uses the feedback renderer's default geometry color.
   *
   * @param from line start
   * @param to line end
   * @param arrow whether to draw an arrow head
   * @param color optional line color override
   */
  default void line(Point from, Point to, boolean arrow, Color color) {
    line(from, to, arrow);
  }

  /**
   * Draws a rectangle using two opposite corners.
   *
   * @param firstCorner first rectangle corner
   * @param secondCorner opposite rectangle corner
   */
  void rectangle(Point firstCorner, Point secondCorner);

  /**
   * Draws a world-space label.
   *
   * @param position label position
   * @param text label text
   */
  void label(Point position, String text);

  /**
   * Draws a region with its two corner points and a label in its center.
   *
   * <p>A region collapsed to a single point is drawn as a single point marker instead, with the
   * label above it.
   *
   * @param region region to draw
   * @param label region label
   */
  default void region(Region region, String label) {
    Point bottomLeft = region.bottomLeft();
    Point topRight = region.topRight();
    if (bottomLeft.equals(topRight)) {
      point(bottomLeft, label);
      return;
    }
    point(bottomLeft, null);
    point(topRight, null);
    rectangle(bottomLeft, topRight);
    label(
        new Point((bottomLeft.x() + topRight.x()) * 0.5f, (bottomLeft.y() + topRight.y()) * 0.5f),
        label);
  }
}
