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
  private static final List<ScreenText> SCREEN_TEXTS = new ArrayList<>();
  private static final List<ScreenMarker> SCREEN_MARKERS = new ArrayList<>();

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
    List<ScreenText> texts;
    List<ScreenMarker> markers;

    synchronized (LOCK) {
      rectangles = new ArrayList<>(WORLD_RECTANGLES);
      texts = new ArrayList<>(SCREEN_TEXTS);
      markers = new ArrayList<>(SCREEN_MARKERS);
      WORLD_RECTANGLES.clear();
      SCREEN_TEXTS.clear();
      SCREEN_MARKERS.clear();
    }

    Graphics2D g = (Graphics2D) base.create();
    try {
      g.setStroke(new BasicStroke(Math.max(1f, view.tilePx() / 16f)));

      int levelHeight =
        view.levelHeight() > 0
          ? view.levelHeight()
          : Game.currentLevel().map(level -> level.layout().length).orElse(0);

      renderWorldRectangles(g, rectangles, view, levelHeight);
      renderScreenMarkers(g, markers);
      renderScreenTexts(g, texts);
    } finally {
      g.dispose();
    }
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

  private record WorldRectangle(float x, float y, float width, float height, Color color) {}

  private record ScreenText(String text, Point screen, Color color) {}

  private record ScreenMarker(Point center, int diameterPx, Color fillColor, Color outlineColor) {}
}
