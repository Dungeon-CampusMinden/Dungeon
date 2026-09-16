package feature.prefabs;

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
