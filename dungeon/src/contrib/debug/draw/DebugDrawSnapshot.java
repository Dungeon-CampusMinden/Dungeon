package contrib.debug.draw;

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
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      List.of());

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
}
