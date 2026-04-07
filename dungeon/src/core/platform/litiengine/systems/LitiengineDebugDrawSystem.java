package core.platform.litiengine.systems;

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
 * Minimal debug draw system for the LITIENGINE backend.
 *
 * <p>This system provides a small free-form debug drawing API similar in intent to the old
 * GDX-based DebugDrawSystem, but implemented on top of the current LITIENGINE Graphics2D
 * render bridge.
 *
 * <p>The API is intentionally small for this first step:
 *
 * <ul>
 *   <li>world-space rectangle outlines
 *   <li>screen-space text
 *   <li>HUD visibility toggle
 * </ul>
 *
 * <p>Draw requests are queued for the next render pass and cleared afterward. This keeps the
 * API independent of the exact caller timing and avoids direct rendering from arbitrary code.
 */
public final class LitiengineDebugDrawSystem extends System {

  private static final Object LOCK = new Object();

  private static final List<WorldRectangle> WORLD_RECTANGLES = new ArrayList<>();
  private static final List<ScreenText> SCREEN_TEXTS = new ArrayList<>();

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
    List<WorldRectangle> rectangles;
    List<ScreenText> texts;

    synchronized (LOCK) {
      rectangles = new ArrayList<>(WORLD_RECTANGLES);
      texts = new ArrayList<>(SCREEN_TEXTS);
      WORLD_RECTANGLES.clear();
      SCREEN_TEXTS.clear();
    }

    if (!hudVisible) {
      return;
    }

    Graphics2D base = LitiengineGraphicsContext.get();
    if (base == null) {
      return;
    }

    Graphics2D g = (Graphics2D) base.create();
    try {
      g.setStroke(new BasicStroke(2f));
      renderWorldRectangles(g, rectangles);
      renderScreenTexts(g, texts);
    } finally {
      g.dispose();
    }
  }

  /**
   * Toggles visibility of the LITIENGINE debug HUD.
   */
  public static void toggleHUD() {
    hudVisible = !hudVisible;
    clearQueuedDrawCalls();
  }

  /**
   * Returns whether the LITIENGINE debug HUD is currently visible.
   *
   * @return true if visible, false otherwise
   */
  public static boolean isHudVisible() {
    return hudVisible;
  }

  /**
   * Queues a world-space rectangle outline for the next render pass.
   *
   * @param x bottom-left world x
   * @param y bottom-left world y
   * @param width width in world units
   * @param height height in world units
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
   * Queues screen-space text for the next render pass.
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

  /**
   * Queues screen-space text in white color for the next render pass.
   *
   * @param text text to draw
   * @param screen screen position in pixels
   */
  public static void drawText(String text, Point screen) {
    drawText(text, screen, Color.WHITE);
  }

  /**
   * Removes all queued draw calls that have not yet been rendered.
   */
  public static void clearQueuedDrawCalls() {
    synchronized (LOCK) {
      WORLD_RECTANGLES.clear();
      SCREEN_TEXTS.clear();
    }
  }

  @Override
  public void stop() {
    // Keep this system effectively always active like the old debug draw system.
    this.run = true;
  }

  @Override
  public void run() {
    this.run = true;
  }

  private static void renderWorldRectangles(Graphics2D g, List<WorldRectangle> rectangles) {
    if (rectangles.isEmpty()) {
      return;
    }

    LitiengineCameraViews.View view = LitiengineCameraViews.get();
    int tilePx = Math.max(1, view.tilePx());
    int levelHeight = view.levelHeight();

    for (WorldRectangle rectangle : rectangles) {
      int screenX = (int) Math.round(view.offsetX() + rectangle.x() * tilePx);
      int screenY =
        levelHeight > 0
          ? (int)
          Math.round(
            view.offsetY() + (levelHeight - rectangle.y() - rectangle.height()) * tilePx)
          : (int) Math.round(view.offsetY() + rectangle.y() * tilePx);

      int screenWidth = Math.max(1, Math.round(rectangle.width() * tilePx));
      int screenHeight = Math.max(1, Math.round(rectangle.height() * tilePx));

      g.setColor(rectangle.color());
      g.drawRect(screenX, screenY, screenWidth, screenHeight);
    }
  }

  private static void renderScreenTexts(Graphics2D g, List<ScreenText> texts) {
    if (texts.isEmpty()) {
      return;
    }

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

  private record WorldRectangle(float x, float y, float width, float height, Color color) {}

  private record ScreenText(String text, Point screen, Color color) {}
}
