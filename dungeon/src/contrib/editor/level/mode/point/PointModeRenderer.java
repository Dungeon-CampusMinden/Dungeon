package contrib.editor.level.mode.point;

import contrib.editor.level.LevelEditorSystem;
import core.camera.CameraViewportState;
import core.utils.Point;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * The PointModeRenderer class provides functionality for rendering named points and the associated
 * graphical markers and labels within the level editor.
 *
 * <p>This class is used to visually represent anchor points or markers on a dungeon level, as well
 * as highlight a held point.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Draws named point markers with customizable colors, sizes, and labels.
 *   <li>Highlights a currently held point with a distinct appearance.
 *   <li>Supports rendering ghost markers for held points being moved.
 * </ul>
 *
 * <p>Features:
 * <ul>
 *   <li>Dynamically scales marker sizes based on tile size, clamped within specified bounds.
 *   <li>Automatically adjusts marker appearance for held points or hovered items.
 * </ul>
 */
final class PointModeRenderer {

  private static final Color POINT_MARKER_COLOR = new Color(255, 196, 77, 220);
  private static final Color HELD_POINT_MARKER_COLOR = new Color(120, 220, 120, 230);
  private static final Color POINT_LABEL_COLOR = Color.WHITE;
  private static final int POINT_MARKER_MIN_PX = 8;
  private static final int POINT_MARKER_MAX_PX = 18;

  private final LevelEditorSystem system;

  PointModeRenderer(LevelEditorSystem system) {
    this.system = system;
  }

  void render(Graphics2D g, String heldPointName, Point snapPos) {
    CameraViewportState.activeViewport()
        .ifPresent(
            view ->
                system
                    .currentDungeonLevelForModes()
                    .ifPresent(
                        level -> {
                          int markerSize =
                              scaledPixelsClamped(
                                  view.tilePx(), 1f / 3f, POINT_MARKER_MIN_PX, POINT_MARKER_MAX_PX);

                          level
                              .namedPoints()
                              .forEach(
                                  (name, pos) ->
                                      drawNamedPointMarker(
                                          g, name, pos, markerSize, heldPointName));

                          if (heldPointName != null) {
                            drawHeldPointGhost(g, heldPointName, snapPos, markerSize);
                          }
                        }));
  }

  private static int scaledPixels(int tilePx, float factor, int minPx) {
    int safeTilePx = Math.max(1, tilePx);
    return Math.max(minPx, Math.round(safeTilePx * factor));
  }

  private static int scaledPixelsClamped(int tilePx, float factor, int minPx, int maxPx) {
    return Math.clamp(scaledPixels(tilePx, factor, minPx), minPx, maxPx);
  }

  private void drawNamedPointMarker(
      Graphics2D g, String name, Point pointPos, int markerSize, String heldPointName) {
    Point screenCenter = CameraViewportState.worldCenterToScreen(pointPos);
    boolean heldPoint = name != null && name.equals(heldPointName);

    drawMarker(
        g, screenCenter, markerSize, heldPoint ? HELD_POINT_MARKER_COLOR : POINT_MARKER_COLOR);

    int radius = markerSize / 2;
    drawLabel(g, name, new Point(screenCenter.x() + radius + 4, screenCenter.y() - 4));
  }

  private void drawHeldPointGhost(Graphics2D g, String name, Point pointPos, int markerSize) {
    Point screenCenter = CameraViewportState.worldCenterToScreen(pointPos);

    drawMarker(g, screenCenter, markerSize, HELD_POINT_MARKER_COLOR);

    int radius = markerSize / 2;
    drawLabel(g, name + " (held)", new Point(screenCenter.x() + radius + 4, screenCenter.y() - 4));
  }

  private void drawMarker(Graphics2D g, Point center, int size, Color fill) {
    int radius = size / 2;
    int x = Math.round(center.x()) - radius;
    int y = Math.round(center.y()) - radius;

    Color old = g.getColor();
    g.setColor(fill);
    g.fillOval(x, y, size, size);
    g.setColor(Color.BLACK);
    g.drawOval(x, y, size, size);
    g.setColor(old);
  }

  private void drawLabel(Graphics2D g, String text, Point pos) {
    Color old = g.getColor();
    g.setColor(POINT_LABEL_COLOR);
    g.drawString(text, Math.round(pos.x()), Math.round(pos.y()));
    g.setColor(old);
  }
}
