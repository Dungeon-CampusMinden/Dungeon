package contrib.debug.draw;

import core.Game;
import core.camera.CameraViewportState;
import core.utils.Point;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Renders immutable debug draw snapshots to a graphics context.
 *
 * <p>The renderer translates queued world-space and screen-space primitives into immediate drawing
 * operations for the current viewport.
 */
public final class DebugDrawRenderer {

  private DebugDrawRenderer() {}

  /**
   * Renders the provided debug draw commands into a derived graphics context.
   *
   * @param base base graphics context to render into
   * @param view active camera viewport used for world-to-screen conversion
   * @param drawCalls immutable draw commands to render
   */
  public static void render(
      Graphics2D base, CameraViewportState.Viewport view, DebugDrawSnapshot drawCalls) {

    if (drawCalls.isEmpty()) {
      return;
    }

    Graphics2D g = (Graphics2D) base.create();
    try {
      g.setStroke(new BasicStroke(scaledStroke(view.tilePx())));

      int levelHeight = resolveLevelHeight(view);

      renderWorldRectangles(g, drawCalls.worldRectangles(), view, levelHeight);
      renderScreenTexts(g, drawCalls.screenTexts());
      renderWorldCircleFills(g, drawCalls.worldCircleFills());
      renderWorldLines(g, drawCalls.worldLines());
      renderWorldCircleOutlines(g, drawCalls.worldCircleOutlines());
      renderScreenRectangles(g, drawCalls.screenRectangles());
    } finally {
      g.dispose();
    }
  }

  private static float scaledStroke(int tilePx) {
    int safeTilePx = Math.max(1, tilePx);
    return Math.max((float) 1.0, safeTilePx * (float) 0.0625);
  }

  private static int resolveLevelHeight(CameraViewportState.Viewport view) {
    if (view.levelHeight() > 0) {
      return view.levelHeight();
    }

    return Game.currentLevel().map(level -> level.layout().length).orElse(0);
  }

  private static void renderWorldRectangles(
      Graphics2D g,
      List<DebugDrawSnapshot.WorldRectangle> rectangles,
      CameraViewportState.Viewport view,
      int levelHeight) {

    int tilePx = view.tilePx();
    for (DebugDrawSnapshot.WorldRectangle rectangle : rectangles) {
      renderWorldRect(
          g,
          view,
          levelHeight,
          tilePx,
          rectangle.x(),
          rectangle.y(),
          rectangle.width(),
          rectangle.height(),
          rectangle.color()
      );
    }
  }

  private static void renderScreenTexts(Graphics2D g, List<DebugDrawSnapshot.ScreenText> texts) {
    FontMetrics metrics = g.getFontMetrics();
    int lineHeight = Math.max(metrics.getHeight(), 14);

    for (DebugDrawSnapshot.ScreenText text : texts) {
      String[] lines = text.text().split("\\R", -1);
      float x = text.screen().x();
      float y = text.screen().y();

      for (int i = 0; i < lines.length; i++) {
        float lineY = y + i * lineHeight;

        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(lines[i], x + 1, lineY + 1);

        g.setColor(text.color());
        g.drawString(lines[i], x, lineY);
      }
    }
  }


  private static void renderWorldRect(
      Graphics2D g,
      CameraViewportState.Viewport view,
      int levelHeight,
      int tilePx,
      float x,
      float y,
      float width,
      float height,
      Color color) {

    int screenX = (int) Math.round(view.offsetX() + x * tilePx);
    int screenY =
        levelHeight > 0
            ? (int) Math.round(view.offsetY() + (levelHeight - y - height) * tilePx)
            : (int) Math.round(view.offsetY() + y * tilePx);

    int screenWidth = Math.max(1, Math.round(width * tilePx));
    int screenHeight = Math.max(1, Math.round(height * tilePx));

    g.setColor(color);
    g.drawRect(screenX, screenY, screenWidth, screenHeight);
  }

  private static void renderWorldLines(Graphics2D g, List<DebugDrawSnapshot.WorldLine> lines) {
    for (DebugDrawSnapshot.WorldLine line : lines) {
      Point from = CameraViewportState.worldToScreen(line.from());
      Point to = CameraViewportState.worldToScreen(line.to());

      g.setColor(line.color());
      g.drawLine(
          Math.round(from.x()), Math.round(from.y()), Math.round(to.x()), Math.round(to.y()));
    }
  }

  private static void renderWorldCircleOutlines(
      Graphics2D g, List<DebugDrawSnapshot.WorldCircleOutline> circles) {
    for (DebugDrawSnapshot.WorldCircleOutline circle : circles) {
      Point center = CameraViewportState.worldToScreen(circle.center());
      int radiusPx = CameraViewportState.worldLengthToScreen(circle.radius());

      g.setColor(circle.color());
      g.drawOval(
          Math.round(center.x()) - radiusPx,
          Math.round(center.y()) - radiusPx,
          radiusPx * 2,
          radiusPx * 2);
    }
  }

  private static void renderWorldCircleFills(
      Graphics2D g, List<DebugDrawSnapshot.WorldCircleFill> circles) {
    for (DebugDrawSnapshot.WorldCircleFill circle : circles) {
      Point center = CameraViewportState.worldToScreen(circle.center());
      int radiusPx = CameraViewportState.worldLengthToScreen(circle.radius());

      g.setColor(circle.color());
      g.fillOval(
          Math.round(center.x()) - radiusPx,
          Math.round(center.y()) - radiusPx,
          radiusPx * 2,
          radiusPx * 2);
    }
  }

  private static void renderScreenRectangles(
      Graphics2D g, List<DebugDrawSnapshot.ScreenRectangle> rectangles) {
    for (DebugDrawSnapshot.ScreenRectangle rect : rectangles) {
      int roundX = Math.round(rect.topLeft().x());
      int roundY = Math.round(rect.topLeft().y());

      if (rect.fill() != null) {
        g.setColor(rect.fill());
        g.fillRect(roundX, roundY, rect.width(), rect.height());
      }

      if (rect.outline() != null) {
        g.setColor(rect.outline());
        g.drawRect(roundX, roundY, rect.width(), rect.height());
      }
    }
  }
}
