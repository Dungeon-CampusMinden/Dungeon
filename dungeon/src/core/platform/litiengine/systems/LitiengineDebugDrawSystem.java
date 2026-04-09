package core.platform.litiengine.systems;

import core.Game;
import core.System;
import core.platform.litiengine.render.LitiengineCameraViews;
import core.platform.litiengine.render.LitiengineGraphicsContext;
import core.utils.Point;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Minimal free-form debug draw layer for the LITIENGINE backend.
 *
 * <p>This system is intentionally lightweight. It allows backend-local code such as the
 * LITIENGINE level editor to enqueue temporary overlay primitives for the current frame:
 *
 * <ul>
 *   <li>world-space rectangle outlines
 *   <li>screen-space text
 * </ul>
 *
 * <p>The actual gameplay/world rendering remains the responsibility of the regular
 * LITIENGINE sprite renderer.
 */
public final class LitiengineDebugDrawSystem extends System {

  private static final Object LOCK = new Object();

  private static final List<WorldRectangle> WORLD_RECTANGLES = new ArrayList<>();
  private static final List<WorldFill> WORLD_FILLS = new ArrayList<>();
  private static final List<ScreenText> SCREEN_TEXTS = new ArrayList<>();
  private static final List<ScreenMarker> SCREEN_MARKERS = new ArrayList<>();
  private static final List<WorldLine> WORLD_LINES = new CopyOnWriteArrayList<>();
  private static final List<WorldCircleOutline> WORLD_CIRCLE_OUTLINES = new CopyOnWriteArrayList<>();
  private static final List<WorldCircleFill> WORLD_CIRCLE_FILLS = new CopyOnWriteArrayList<>();
  private static final List<ScreenRectangle> SCREEN_RECTANGLES = new CopyOnWriteArrayList<>();

  private static volatile boolean hudVisible = true;

  public LitiengineDebugDrawSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    // render-only system
  }

  @Override
  public void render(float deltaSeconds) {
    Graphics2D base = LitiengineGraphicsContext.get();
    if (base == null) {
      clearQueuedDrawCalls();
      return;
    }

    if (!hudVisible) {
      clearQueuedDrawCalls();
      return;
    }

    LitiengineCameraViews.View view = LitiengineCameraViews.get();
    if (view == null || view.tilePx() <= 0) {
      clearQueuedDrawCalls();
      return;
    }

    List<WorldRectangle> rectangles;
    List<WorldFill> fills;
    List<ScreenText> texts;
    List<ScreenMarker> markers;

    synchronized (LOCK) {
      rectangles = new ArrayList<>(WORLD_RECTANGLES);
      fills = new ArrayList<>(WORLD_FILLS);
      texts = new ArrayList<>(SCREEN_TEXTS);
      markers = new ArrayList<>(SCREEN_MARKERS);
      WORLD_RECTANGLES.clear();
      WORLD_FILLS.clear();
      SCREEN_TEXTS.clear();
      SCREEN_MARKERS.clear();
    }

    renderQueuedDrawCalls(base, rectangles, fills, texts, markers);
  }

  public static void toggleHUD() {
    hudVisible = !hudVisible;
    clearQueuedDrawCalls();
  }

  public static boolean isHudVisible() {
    return hudVisible;
  }

  public static void clearQueuedDrawCalls() {
    synchronized (LOCK) {
      WORLD_RECTANGLES.clear();
      WORLD_FILLS.clear();
      SCREEN_TEXTS.clear();
      SCREEN_MARKERS.clear();
    }
  }

  /**
   * Queues a world-space rectangle outline for the current frame.
   *
   * @param x world x of bottom-left corner
   * @param y world y of bottom-left corner
   * @param width world width
   * @param height world height
   * @param color outline color
   */
  public static void drawRectangleOutline(
    float x, float y, float width, float height, Color color) {

    if (width <= 0 || height <= 0) {
      return;
    }

    synchronized (LOCK) {
      WORLD_RECTANGLES.add(
        new WorldRectangle(x, y, width, height, color == null ? Color.WHITE : color));
    }
  }

  /**
   * Queues screen-space text for the current frame.
   *
   * @param text text to draw
   * @param screen screen position in pixels
   * @param color text color
   */
  public static void drawText(String text, Point screen, Color color) {
    Objects.requireNonNull(text, "text must not be null");
    Objects.requireNonNull(screen, "screen must not be null");

    synchronized (LOCK) {
      SCREEN_TEXTS.add(new ScreenText(text, screen, color == null ? Color.WHITE : color));
    }
  }

  public static void drawText(String text, Point screen) {
    drawText(text, screen, Color.WHITE);
  }

  /**
   * Queues a filled screen-space marker circle for the current frame.
   *
   * @param center center position in screen pixels
   * @param diameterPx marker diameter in pixels
   * @param fillColor fill color, may be null
   * @param outlineColor outline color, may be null
   */
  public static void drawScreenMarker(
    Point center, int diameterPx, Color fillColor, Color outlineColor) {

    Objects.requireNonNull(center, "center must not be null");

    if (diameterPx <= 0) {
      return;
    }

    synchronized (LOCK) {
      SCREEN_MARKERS.add(new ScreenMarker(center, diameterPx, fillColor, outlineColor));
    }
  }

  @Override
  public void stop() {
    this.run = true;
  }

  @Override
  public void run() {
    this.run = true;
  }

  private static void renderWorldRectangles(
    Graphics2D g,
    List<WorldRectangle> rectangles,
    LitiengineCameraViews.View view,
    int levelHeight) {

    int tilePx = view.tilePx();

    for (WorldRectangle rectangle : rectangles) {
      int screenX = (int) Math.round(view.offsetX() + rectangle.x() * tilePx);

      int screenY =
        levelHeight > 0
          ? (int)
          Math.round(
            view.offsetY()
            + (levelHeight - rectangle.y() - rectangle.height()) * tilePx)
          : (int) Math.round(view.offsetY() + rectangle.y() * tilePx);

      int screenWidth = Math.max(1, Math.round(rectangle.width() * tilePx));
      int screenHeight = Math.max(1, Math.round(rectangle.height() * tilePx));

      g.setColor(rectangle.color());
      g.drawRect(screenX, screenY, screenWidth, screenHeight);
    }
  }

  private static void renderScreenTexts(Graphics2D g, List<ScreenText> texts) {
    FontMetrics metrics = g.getFontMetrics();
    int lineHeight = Math.max(metrics.getHeight(), 14);

    for (ScreenText text : texts) {
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

  private static void renderScreenMarkers(Graphics2D g, List<ScreenMarker> markers) {
    if (markers.isEmpty()) {
      return;
    }

    for (ScreenMarker marker : markers) {
      int radius = marker.diameterPx() / 2;
      int x = Math.round(marker.center().x()) - radius;
      int y = Math.round(marker.center().y()) - radius;

      if (marker.fillColor() != null) {
        g.setColor(marker.fillColor());
        g.fillOval(x, y, marker.diameterPx(), marker.diameterPx());
      }

      if (marker.outlineColor() != null) {
        g.setColor(marker.outlineColor());
        g.drawOval(x, y, marker.diameterPx(), marker.diameterPx());
      }
    }
  }

  private static void renderWorldFills(
    Graphics2D g,
    List<WorldFill> fills,
    LitiengineCameraViews.View view,
    int levelHeight) {

    int tilePx = view.tilePx();

    for (WorldFill fill : fills) {
      int screenX = (int) Math.round(view.offsetX() + fill.x() * tilePx);

      int screenY =
        levelHeight > 0
          ? (int)
          Math.round(
            view.offsetY() + (levelHeight - fill.y() - fill.height()) * tilePx)
          : (int) Math.round(view.offsetY() + fill.y() * tilePx);

      int screenWidth = Math.max(1, Math.round(fill.width() * tilePx));
      int screenHeight = Math.max(1, Math.round(fill.height() * tilePx));

      g.setColor(fill.color());
      g.fillRect(screenX, screenY, screenWidth, screenHeight);
    }
  }

  private void renderWorldLines(java.awt.Graphics2D g) {
    List<WorldLine> lines = new ArrayList<>(WORLD_LINES);
    WORLD_LINES.clear();

    for (WorldLine line : lines) {
      Point from = LitiengineCameraViews.worldToScreen(line.from());
      Point to = LitiengineCameraViews.worldToScreen(line.to());

      g.setColor(line.color());
      g.drawLine(
        Math.round(from.x()),
        Math.round(from.y()),
        Math.round(to.x()),
        Math.round(to.y()));
    }
  }

  private void renderWorldCircleOutlines(java.awt.Graphics2D g) {
    List<WorldCircleOutline> circles = new ArrayList<>(WORLD_CIRCLE_OUTLINES);
    WORLD_CIRCLE_OUTLINES.clear();

    for (WorldCircleOutline circle : circles) {
      Point center = LitiengineCameraViews.worldToScreen(circle.center());
      int radiusPx = LitiengineCameraViews.worldLengthToScreen(circle.radius());

      g.setColor(circle.color());
      g.drawOval(
        Math.round(center.x()) - radiusPx,
        Math.round(center.y()) - radiusPx,
        radiusPx * 2,
        radiusPx * 2);
    }
  }

  private void renderWorldCircleFills(java.awt.Graphics2D g) {
    List<WorldCircleFill> circles = new ArrayList<>(WORLD_CIRCLE_FILLS);
    WORLD_CIRCLE_FILLS.clear();

    for (WorldCircleFill circle : circles) {
      Point center = LitiengineCameraViews.worldToScreen(circle.center());
      int radiusPx = LitiengineCameraViews.worldLengthToScreen(circle.radius());

      g.setColor(circle.color());
      g.fillOval(
        Math.round(center.x()) - radiusPx,
        Math.round(center.y()) - radiusPx,
        radiusPx * 2,
        radiusPx * 2);
    }
  }

  private void renderScreenRectangles(java.awt.Graphics2D g) {
    List<ScreenRectangle> rectangles = new ArrayList<>(SCREEN_RECTANGLES);
    SCREEN_RECTANGLES.clear();

    for (ScreenRectangle rect : rectangles) {
      if (rect.fill() != null) {
        g.setColor(rect.fill());
        g.fillRect(
          Math.round(rect.topLeft().x()),
          Math.round(rect.topLeft().y()),
          rect.width(),
          rect.height());
      }

      if (rect.outline() != null) {
        g.setColor(rect.outline());
        g.drawRect(
          Math.round(rect.topLeft().x()),
          Math.round(rect.topLeft().y()),
          rect.width(),
          rect.height());
      }
    }
  }

  private void renderQueuedDrawCalls(
    Graphics2D base,
    List<WorldRectangle> rectangles,
    List<WorldFill> fills,
    List<ScreenText> texts,
    List<ScreenMarker> markers) {

    LitiengineCameraViews.activeView()
      .ifPresent(
        view -> {
          Graphics2D g = (Graphics2D) base.create();
          try {
            g.setStroke(new BasicStroke(Math.max(1f, view.tilePx() / 16f)));

            int levelHeight =
              view.levelHeight() > 0
                ? view.levelHeight()
                : Game.currentLevel().map(level -> level.layout().length).orElse(0);

            renderWorldFills(g, fills, view, levelHeight);
            renderWorldRectangles(g, rectangles, view, levelHeight);
            renderScreenMarkers(g, markers);
            renderScreenTexts(g, texts);
            renderWorldCircleFills(g);
            renderWorldLines(g);
            renderWorldCircleOutlines(g);
            renderScreenRectangles(g);
          } finally {
            g.dispose();
          }
        });
  }

  /**
   * Queues a filled world-space rectangle for the current frame.
   *
   * @param x world x of bottom-left corner
   * @param y world y of bottom-left corner
   * @param width world width
   * @param height world height
   * @param color fill color
   */
  public static void fillWorldRectangle(
    float x, float y, float width, float height, Color color) {

    if (width <= 0 || height <= 0) {
      return;
    }

    synchronized (LOCK) {
      WORLD_FILLS.add(new WorldFill(x, y, width, height, color == null ? Color.WHITE : color));
    }
  }

  public static void drawWorldLine(Point from, Point to, Color color) {
    if (from == null || to == null || color == null) return;
    WORLD_LINES.add(new WorldLine(from, to, color));
  }

  public static void drawWorldCircleOutline(
    Point center, float radius, Color color) {
    if (center == null || color == null || radius <= 0f) return;
    WORLD_CIRCLE_OUTLINES.add(new WorldCircleOutline(center, radius, color));
  }

  public static void drawWorldCircleFill(
    Point center, float radius, Color color) {
    if (center == null || color == null || radius <= 0f) return;
    WORLD_CIRCLE_FILLS.add(new WorldCircleFill(center, radius, color));
  }

  public static void drawScreenRectangle(
    Point topLeft, int width, int height, Color fill, Color outline) {
    if (topLeft == null || width <= 0 || height <= 0) return;
    SCREEN_RECTANGLES.add(new ScreenRectangle(topLeft, width, height, fill, outline));
  }

  private record WorldRectangle(float x, float y, float width, float height, Color color) {}

  private record WorldFill(float x, float y, float width, float height, Color color) {}

  private record ScreenText(String text, Point screen, Color color) {}

  private record ScreenMarker(Point center, int diameterPx, Color fillColor, Color outlineColor) {}

  private record WorldLine(Point from, Point to, Color color) {}

  private record WorldCircleOutline(Point center, float radius, Color color) {}

  private record WorldCircleFill(Point center, float radius, Color color) {}

  private record ScreenRectangle(Point topLeft, int width, int height, Color fill, Color outline) {}
}
