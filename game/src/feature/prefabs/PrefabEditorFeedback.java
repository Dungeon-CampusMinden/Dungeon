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
}
