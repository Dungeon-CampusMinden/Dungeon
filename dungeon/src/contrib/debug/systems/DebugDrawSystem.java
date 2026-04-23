package contrib.debug.systems;

import core.Game;
import core.System;
import core.camera.CameraViewportState;
import core.game.render.RenderContext;
import core.game.render.TileOverlaySizing;
import core.utils.Point;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A system for rendering debug visualization primitives.
 *
 * <p>This system manages queues of debug draw calls (rectangles, lines, circles, text, markers)
 * in both world-space and screen-space coordinates.
 *
 * <p>It provides static methods to queue draw calls that are rendered each frame if the debug HUD is visible.
 * All queued calls are cleared after rendering.
 */
public final class DebugDrawSystem extends System {

  private static final DebugDrawQueue DRAW_QUEUE = new DebugDrawQueue();

  private static final AtomicBoolean HUD_VISIBLE = new AtomicBoolean(false);

  /** Creates a new debug draw system. */
  public DebugDrawSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    // render-only system
  }

  @Override
  public void render(float deltaSeconds) {
    Graphics2D base = RenderContext.get();
    if (base == null) {
      clearQueuedDrawCalls();
      return;
    }

    if (!isHudVisible()) {
      clearQueuedDrawCalls();
      return;
    }

    CameraViewportState.Viewport view = CameraViewportState.get();
    if (view == null || view.tilePx() <= 0) {
      clearQueuedDrawCalls();
      return;
    }

    renderQueuedDrawCalls(base, DRAW_QUEUE.snapshotAndClear());
  }

  /**
   * Toggles the visibility of the debug HUD.
   *
   * <p>When toggled off, all queued draw calls are cleared.
   */
  public static void toggleHUD() {
    boolean visible;
    do {
      visible = HUD_VISIBLE.get();
    } while (!HUD_VISIBLE.compareAndSet(visible, !visible));
    clearQueuedDrawCalls();
  }

  /**
   * Checks whether the debug HUD is currently visible.
   *
   * @return true if the debug HUD is visible, false otherwise
   */
  public static boolean isHudVisible() {
    return HUD_VISIBLE.get();
  }

  /**
   * Clears all queued debug draw calls.
   */
  public static void clearQueuedDrawCalls() {
    DRAW_QUEUE.clear();
  }

  /**
   * Queues a world-space rectangle outline for the current frame.
   *
   * @param x world x of a bottom-left corner
   * @param y world y of a bottom-left corner
   * @param width world width
   * @param height world height
   * @param color outline color
   */
  public static void drawRectangleOutline(
    float x, float y, float width, float height, Color color) {

    if (width <= 0 || height <= 0) {
      return;
    }

    DRAW_QUEUE.addWorldRectangle(
      new WorldRectangle(x, y, width, height, color == null ? Color.WHITE : color));
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

    DRAW_QUEUE.addScreenText(new ScreenText(text, screen, color == null ? Color.WHITE : color));
  }

  /**
   * Queues a filled screen-space marker circle for the current frame.
   *
   * @param center center position in screen pixels
   * @param diameterPx marker diameter in pixels
   * @param fillColor fill color, may be null
   * @param outlineColor outline color may be null
   */
  public static void drawScreenMarker(
    Point center, int diameterPx, Color fillColor, Color outlineColor) {

    Objects.requireNonNull(center, "center must not be null");

    if (diameterPx <= 0) {
      return;
    }

    DRAW_QUEUE.addScreenMarker(new ScreenMarker(center, diameterPx, fillColor, outlineColor));
  }

  @Override
  public void stop() {
    // Debug draw remains active so queued calls are drained even during gameplay pauses.
  }

  @Override
  public void run() {
    this.run = true;
  }

  private static void renderWorldRectangles(
    Graphics2D g,
    List<WorldRectangle> rectangles,
    CameraViewportState.Viewport view,
    int levelHeight) {

    int tilePx = view.tilePx();

    for (WorldRectangle rectangle : rectangles) {
      renderWorldRect(
        g, view, levelHeight, tilePx,
        rectangle.x(), rectangle.y(), rectangle.width(), rectangle.height(),
        rectangle.color(), false);
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
    CameraViewportState.Viewport view,
    int levelHeight) {

    int tilePx = view.tilePx();

    for (WorldFill fill : fills) {
      renderWorldRect(
        g, view, levelHeight, tilePx,
        fill.x(), fill.y(), fill.width(), fill.height(),
        fill.color(), true);
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
    Color color,
    boolean fill) {

    int screenX = (int) Math.round(view.offsetX() + x * tilePx);

    int screenY =
      levelHeight > 0
        ? (int) Math.round(view.offsetY() + (levelHeight - y - height) * tilePx)
        : (int) Math.round(view.offsetY() + y * tilePx);

    int screenWidth = Math.max(1, Math.round(width * tilePx));
    int screenHeight = Math.max(1, Math.round(height * tilePx));

    g.setColor(color);
    if (fill) {
      g.fillRect(screenX, screenY, screenWidth, screenHeight);
    } else {
      g.drawRect(screenX, screenY, screenWidth, screenHeight);
    }
  }

  private static void renderWorldLines(Graphics2D g, List<WorldLine> lines) {
    for (WorldLine line : lines) {
      Point from = CameraViewportState.worldToScreen(line.from());
      Point to = CameraViewportState.worldToScreen(line.to());

      g.setColor(line.color());
      g.drawLine(
        Math.round(from.x()),
        Math.round(from.y()),
        Math.round(to.x()),
        Math.round(to.y()));
    }
  }

  private static void renderWorldCircleOutlines(Graphics2D g, List<WorldCircleOutline> circles) {
    for (WorldCircleOutline circle : circles) {
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

  private static void renderWorldCircleFills(Graphics2D g, List<WorldCircleFill> circles) {
    for (WorldCircleFill circle : circles) {
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

  private static void renderScreenRectangles(Graphics2D g, List<ScreenRectangle> rectangles) {
    for (ScreenRectangle rect : rectangles) {
      int roundX = Math.round(rect.topLeft().x());
      int roundY = Math.round(rect.topLeft().y());
      if (rect.fill() != null) {
        g.setColor(rect.fill());
        g.fillRect(
          roundX,
          roundY,
          rect.width(),
          rect.height());
      }

      if (rect.outline() != null) {
        g.setColor(rect.outline());
        g.drawRect(
          roundX,
          roundY,
          rect.width(),
          rect.height());
      }
    }
  }

  private void renderQueuedDrawCalls(
    Graphics2D base,
    DebugDrawSnapshot queuedDrawCalls) {

    CameraViewportState.activeViewport()
      .ifPresent(
        view -> {
          Graphics2D g = (Graphics2D) base.create();
          try {
            g.setStroke(
              new BasicStroke(TileOverlaySizing.scaledStroke(view.tilePx(), 1f / 16f, 1f)));

            int levelHeight =
              view.levelHeight() > 0
                ? view.levelHeight()
                : Game.currentLevel().map(level -> level.layout().length).orElse(0);

            renderWorldFills(g, queuedDrawCalls.worldFills(), view, levelHeight);
            renderWorldRectangles(g, queuedDrawCalls.worldRectangles(), view, levelHeight);
            renderScreenMarkers(g, queuedDrawCalls.screenMarkers());
            renderScreenTexts(g, queuedDrawCalls.screenTexts());
            renderWorldCircleFills(g, queuedDrawCalls.worldCircleFills());
            renderWorldLines(g, queuedDrawCalls.worldLines());
            renderWorldCircleOutlines(g, queuedDrawCalls.worldCircleOutlines());
            renderScreenRectangles(g, queuedDrawCalls.screenRectangles());
          } finally {
            g.dispose();
          }
        });
  }

  /**
   * Queues a filled world-space rectangle for the current frame.
   *
   * @param x world x of a bottom-left corner
   * @param y world y of a bottom-left corner
   * @param width world width
   * @param height world height
   * @param color fill color
   */
  public static void fillWorldRectangle(
    float x, float y, float width, float height, Color color) {

    if (width <= 0 || height <= 0) {
      return;
    }

    DRAW_QUEUE.addWorldFill(
      new WorldFill(x, y, width, height, color == null ? Color.WHITE : color));
  }

  /**
   * Queues a world-space line for the current frame.
   *
   * @param from start position in world coordinates
   * @param to end position in world coordinates
   * @param color line color
   */
  public static void drawWorldLine(Point from, Point to, Color color) {
    if (from == null || to == null || color == null) {
      return;
    }

    DRAW_QUEUE.addWorldLine(new WorldLine(from, to, color));
  }

  /**
   * Queues a world-space circle outline for the current frame.
   *
   * @param center center position in world coordinates
   * @param radius circle radius in world units
   * @param color outline color
   */
  public static void drawWorldCircleOutline(
    Point center, float radius, Color color) {
    if (center == null || color == null || radius <= 0f) {
      return;
    }

    DRAW_QUEUE.addWorldCircleOutline(new WorldCircleOutline(center, radius, color));
  }

  /**
   * Queues a filled world-space circle for the current frame.
   *
   * @param center center position in world coordinates
   * @param radius circle radius in world units
   * @param color fill color
   */
  public static void drawWorldCircleFill(
    Point center, float radius, Color color) {
    if (center == null || color == null || radius <= 0f) {
      return;
    }

    DRAW_QUEUE.addWorldCircleFill(new WorldCircleFill(center, radius, color));
  }

  /**
   * Queues a filled screen-space rectangle for the current frame.
   *
   * @param topLeft top-left corner in screen pixels
   * @param width rectangle width in pixels
   * @param height rectangle height in pixels
   * @param fill fill color, may be null for no fill
   * @param outline outline color may be null for no outline
   */
  public static void drawScreenRectangle(
    Point topLeft, int width, int height, Color fill, Color outline) {
    if (topLeft == null || width <= 0 || height <= 0) {
      return;
    }

    DRAW_QUEUE.addScreenRectangle(new ScreenRectangle(topLeft, width, height, fill, outline));
  }

  private static final class DebugDrawQueue {
    private final List<WorldRectangle> worldRectangles = new ArrayList<>();
    private final List<WorldFill> worldFills = new ArrayList<>();
    private final List<ScreenText> screenTexts = new ArrayList<>();
    private final List<ScreenMarker> screenMarkers = new ArrayList<>();
    private final List<WorldLine> worldLines = new ArrayList<>();
    private final List<WorldCircleOutline> worldCircleOutlines = new ArrayList<>();
    private final List<WorldCircleFill> worldCircleFills = new ArrayList<>();
    private final List<ScreenRectangle> screenRectangles = new ArrayList<>();

    synchronized void addWorldRectangle(WorldRectangle rectangle) {
      worldRectangles.add(rectangle);
    }

    synchronized void addWorldFill(WorldFill fill) {
      worldFills.add(fill);
    }

    synchronized void addScreenText(ScreenText text) {
      screenTexts.add(text);
    }

    synchronized void addScreenMarker(ScreenMarker marker) {
      screenMarkers.add(marker);
    }

    synchronized void addWorldLine(WorldLine line) {
      worldLines.add(line);
    }

    synchronized void addWorldCircleOutline(WorldCircleOutline circle) {
      worldCircleOutlines.add(circle);
    }

    synchronized void addWorldCircleFill(WorldCircleFill circle) {
      worldCircleFills.add(circle);
    }

    synchronized void addScreenRectangle(ScreenRectangle rectangle) {
      screenRectangles.add(rectangle);
    }

    synchronized DebugDrawSnapshot snapshotAndClear() {
      DebugDrawSnapshot snapshot =
        new DebugDrawSnapshot(
          List.copyOf(worldRectangles),
          List.copyOf(worldFills),
          List.copyOf(screenTexts),
          List.copyOf(screenMarkers),
          List.copyOf(worldLines),
          List.copyOf(worldCircleOutlines),
          List.copyOf(worldCircleFills),
          List.copyOf(screenRectangles));
      clear();
      return snapshot;
    }

    synchronized void clear() {
      worldRectangles.clear();
      worldFills.clear();
      screenTexts.clear();
      screenMarkers.clear();
      worldLines.clear();
      worldCircleOutlines.clear();
      worldCircleFills.clear();
      screenRectangles.clear();
    }

    synchronized boolean isEmpty() {
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

  private record DebugDrawSnapshot(
    List<WorldRectangle> worldRectangles,
    List<WorldFill> worldFills,
    List<ScreenText> screenTexts,
    List<ScreenMarker> screenMarkers,
    List<WorldLine> worldLines,
    List<WorldCircleOutline> worldCircleOutlines,
    List<WorldCircleFill> worldCircleFills,
    List<ScreenRectangle> screenRectangles) {}

  private record WorldRectangle(float x, float y, float width, float height, Color color) {}

  private record WorldFill(float x, float y, float width, float height, Color color) {}

  private record ScreenText(String text, Point screen, Color color) {}

  private record ScreenMarker(Point center, int diameterPx, Color fillColor, Color outlineColor) {}

  private record WorldLine(Point from, Point to, Color color) {}

  private record WorldCircleOutline(Point center, float radius, Color color) {}

  private record WorldCircleFill(Point center, float radius, Color color) {}

  private record ScreenRectangle(Point topLeft, int width, int height, Color fill, Color outline) {}
}
