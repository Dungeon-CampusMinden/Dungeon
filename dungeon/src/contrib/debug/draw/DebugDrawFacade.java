package contrib.debug.draw;

import core.utils.Point;
import java.awt.Color;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Public entry point for transient debug draw primitives and HUD visibility state.
 *
 * <p>The static methods enqueue draw calls for the next rendered frame.
 */
public final class DebugDrawFacade {

  private static final DebugDrawQueue DRAW_QUEUE = new DebugDrawQueue();
  private static final AtomicBoolean HUD_VISIBLE = new AtomicBoolean(false);

  private DebugDrawFacade() {}

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

  /** Clears all queued debug draw calls. */
  public static void clearQueuedDrawCalls() {
    DRAW_QUEUE.clear();
  }

  /**
   * Returns the queued draw commands and clears the queue.
   *
   * @return immutable snapshot of queued draw commands
   */
  public static DebugDrawSnapshot snapshotAndClear() {
    return DRAW_QUEUE.snapshotAndClear();
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

    DRAW_QUEUE.addWorldRectangle(x, y, width, height, color == null ? Color.WHITE : color);
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

    DRAW_QUEUE.addScreenText(text, screen, color == null ? Color.WHITE : color);
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

    DRAW_QUEUE.addWorldLine(from, to, color);
  }

  /**
   * Queues a world-space circle outline for the current frame.
   *
   * @param center center position in world coordinates
   * @param radius circle radius in world units
   * @param color outline color
   */
  public static void drawWorldCircleOutline(Point center, float radius, Color color) {
    if (center == null || color == null || radius <= 0f) {
      return;
    }

    DRAW_QUEUE.addWorldCircleOutline(center, radius, color);
  }

  /**
   * Queues a filled world-space circle for the current frame.
   *
   * @param center center position in world coordinates
   * @param radius circle radius in world units
   * @param color fill color
   */
  public static void drawWorldCircleFill(Point center, float radius, Color color) {
    if (center == null || color == null || radius <= 0f) {
      return;
    }

    DRAW_QUEUE.addWorldCircleFill(center, radius, color);
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

    DRAW_QUEUE.addScreenRectangle(topLeft, width, height, fill, outline);
  }
}
