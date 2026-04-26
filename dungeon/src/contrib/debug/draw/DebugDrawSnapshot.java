package contrib.debug.draw;

import core.utils.Point;
import java.awt.Color;
import java.util.List;

/**
 * Immutable collection of debug draw commands captured for a single render pass.
 *
 * @param worldRectangles queued world-space rectangle outlines
 * @param worldFills queued filled world-space rectangles
 * @param screenTexts queued screen-space text labels
 * @param screenMarkers queued screen-space marker circles
 * @param worldLines queued world-space lines
 * @param worldCircleOutlines queued world-space circle outlines
 * @param worldCircleFills queued filled world-space circles
 * @param screenRectangles queued screen-space rectangles
 */
public record DebugDrawSnapshot(
    List<WorldRectangle> worldRectangles,
    List<WorldFill> worldFills,
    List<ScreenText> screenTexts,
    List<ScreenMarker> screenMarkers,
    List<WorldLine> worldLines,
    List<WorldCircleOutline> worldCircleOutlines,
    List<WorldCircleFill> worldCircleFills,
    List<ScreenRectangle> screenRectangles) {

  private static final DebugDrawSnapshot EMPTY =
      new DebugDrawSnapshot(
          List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

  static DebugDrawSnapshot empty() {
    return EMPTY;
  }

  boolean isEmpty() {
    return worldRectangles.isEmpty()
        && worldFills.isEmpty()
        && screenTexts.isEmpty()
        && screenMarkers.isEmpty()
        && worldLines.isEmpty()
        && worldCircleOutlines.isEmpty()
        && worldCircleFills.isEmpty()
        && screenRectangles.isEmpty();
  }

  /**
   * World-space rectangle outline draw command.
   *
   * @param x world x-coordinate of the bottom-left corner
   * @param y world y-coordinate of the bottom-left corner
   * @param width rectangle width in world units
   * @param height rectangle height in world units
   * @param color outline color
   */
  record WorldRectangle(float x, float y, float width, float height, Color color) {}

  /**
   * Filled world-space rectangle draw command.
   *
   * @param x world x-coordinate of the bottom-left corner
   * @param y world y-coordinate of the bottom-left corner
   * @param width rectangle width in world units
   * @param height rectangle height in world units
   * @param color fill color
   */
  record WorldFill(float x, float y, float width, float height, Color color) {}

  /**
   * Screen-space text draw command.
   *
   * @param text text to render
   * @param screen screen position in pixels
   * @param color text color
   */
  record ScreenText(String text, Point screen, Color color) {}

  /**
   * Screen-space circular marker draw command.
   *
   * @param center center position in screen pixels
   * @param diameterPx marker diameter in pixels
   * @param fillColor fill color, or {@code null} for no fill
   * @param outlineColor outline color, or {@code null} for no outline
   */
  record ScreenMarker(Point center, int diameterPx, Color fillColor, Color outlineColor) {}

  /**
   * World-space line draw command.
   *
   * @param from line start position in world coordinates
   * @param to line end position in world coordinates
   * @param color line color
   */
  record WorldLine(Point from, Point to, Color color) {}

  /**
   * World-space circle outline draw command.
   *
   * @param center circle center in world coordinates
   * @param radius circle radius in world units
   * @param color outline color
   */
  record WorldCircleOutline(Point center, float radius, Color color) {}

  /**
   * Filled world-space circle draw command.
   *
   * @param center circle center in world coordinates
   * @param radius circle radius in world units
   * @param color fill color
   */
  record WorldCircleFill(Point center, float radius, Color color) {}

  /**
   * Screen-space rectangle draw command.
   *
   * @param topLeft top-left corner in screen pixels
   * @param width rectangle width in pixels
   * @param height rectangle height in pixels
   * @param fill fill color, or {@code null} for no fill
   * @param outline outline color, or {@code null} for no outline
   */
  record ScreenRectangle(Point topLeft, int width, int height, Color fill, Color outline) {}
}
