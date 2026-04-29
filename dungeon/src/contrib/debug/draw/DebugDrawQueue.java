package contrib.debug.draw;

import core.utils.Point;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe accumulator for debug draw commands collected during a frame.
 *
 * <p>The queue stores transient primitives until a renderer snapshots and clears them.
 */
public final class DebugDrawQueue {
  private final List<DebugDrawSnapshot.WorldRectangle> worldRectangles = new ArrayList<>();
  private final List<DebugDrawSnapshot.ScreenText> screenTexts = new ArrayList<>();
  private final List<DebugDrawSnapshot.WorldLine> worldLines = new ArrayList<>();
  private final List<DebugDrawSnapshot.WorldCircleOutline> worldCircleOutlines = new ArrayList<>();
  private final List<DebugDrawSnapshot.WorldCircleFill> worldCircleFills = new ArrayList<>();
  private final List<DebugDrawSnapshot.ScreenRectangle> screenRectangles = new ArrayList<>();

  /**
   * Adds a world-space rectangle outline draw command.
   *
   * @param x world x-coordinate of the bottom-left corner
   * @param y world y-coordinate of the bottom-left corner
   * @param width rectangle width in world units
   * @param height rectangle height in world units
   * @param color outline color
   */
  public synchronized void addWorldRectangle(
      float x, float y, float width, float height, Color color) {
    worldRectangles.add(new DebugDrawSnapshot.WorldRectangle(x, y, width, height, color));
  }

  /**
   * Adds a screen-space text draw command.
   *
   * @param text text to render
   * @param screen screen position in pixels
   * @param color text color
   */
  public synchronized void addScreenText(String text, Point screen, Color color) {
    screenTexts.add(new DebugDrawSnapshot.ScreenText(text, screen, color));
  }

  /**
   * Adds a world-space line draw command.
   *
   * @param from line start position in world coordinates
   * @param to line end position in world coordinates
   * @param color line color
   */
  public synchronized void addWorldLine(Point from, Point to, Color color) {
    worldLines.add(new DebugDrawSnapshot.WorldLine(from, to, color));
  }

  /**
   * Adds a world-space circle outline draw command.
   *
   * @param center circle center in world coordinates
   * @param radius circle radius in world units
   * @param color outline color
   */
  public synchronized void addWorldCircleOutline(Point center, float radius, Color color) {
    worldCircleOutlines.add(new DebugDrawSnapshot.WorldCircleOutline(center, radius, color));
  }

  /**
   * Adds a filled world-space circle draw command.
   *
   * @param center circle center in world coordinates
   * @param radius circle radius in world units
   * @param color fill color
   */
  public synchronized void addWorldCircleFill(Point center, float radius, Color color) {
    worldCircleFills.add(new DebugDrawSnapshot.WorldCircleFill(center, radius, color));
  }

  /**
   * Adds a screen-space rectangle draw command.
   *
   * @param topLeft top-left corner in screen pixels
   * @param width rectangle width in pixels
   * @param height rectangle height in pixels
   * @param fill fill color, or {@code null} for no fill
   * @param outline outline color, or {@code null} for no outline
   */
  public synchronized void addScreenRectangle(
      Point topLeft, int width, int height, Color fill, Color outline) {
    screenRectangles.add(
        new DebugDrawSnapshot.ScreenRectangle(topLeft, width, height, fill, outline));
  }

  /**
   * Returns an immutable snapshot of the currently queued commands and clears the queue.
   *
   * @return snapshot of the queued draw commands
   */
  public synchronized DebugDrawSnapshot snapshotAndClear() {
    if (isEmpty()) {
      return DebugDrawSnapshot.empty();
    }

    DebugDrawSnapshot snapshot =
        new DebugDrawSnapshot(
            List.copyOf(worldRectangles),
            List.copyOf(screenTexts),
            List.copyOf(worldLines),
            List.copyOf(worldCircleOutlines),
            List.copyOf(worldCircleFills),
            List.copyOf(screenRectangles));
    clear();
    return snapshot;
  }

  /** Removes all queued draw commands. */
  public synchronized void clear() {
    worldRectangles.clear();
    screenTexts.clear();
    worldLines.clear();
    worldCircleOutlines.clear();
    worldCircleFills.clear();
    screenRectangles.clear();
  }

  /**
   * Checks whether the queue currently contains any draw commands.
   *
   * @return {@code true} if the queue is empty, otherwise {@code false}
   */
  public synchronized boolean isEmpty() {
    return worldRectangles.isEmpty()
        && screenTexts.isEmpty()
        && worldLines.isEmpty()
        && worldCircleOutlines.isEmpty()
        && worldCircleFills.isEmpty()
        && screenRectangles.isEmpty();
  }
}
