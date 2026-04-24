package contrib.debug.draw;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe accumulator for debug draw commands collected during a frame.
 *
 * <p>The queue stores transient primitives until a renderer snapshots and clears them.
 */
public final class DebugDrawQueue {

  /** Creates an empty debug draw queue. */
  public DebugDrawQueue() {}

  private final List<WorldRectangle> worldRectangles = new ArrayList<>();
  private final List<WorldFill> worldFills = new ArrayList<>();
  private final List<ScreenText> screenTexts = new ArrayList<>();
  private final List<ScreenMarker> screenMarkers = new ArrayList<>();
  private final List<WorldLine> worldLines = new ArrayList<>();
  private final List<WorldCircleOutline> worldCircleOutlines = new ArrayList<>();
  private final List<WorldCircleFill> worldCircleFills = new ArrayList<>();
  private final List<ScreenRectangle> screenRectangles = new ArrayList<>();

  /**
   * Adds a world-space rectangle outline draw command.
   *
   * @param rectangle rectangle command to enqueue
   */
  public synchronized void addWorldRectangle(WorldRectangle rectangle) {
    worldRectangles.add(rectangle);
  }

  /**
   * Adds a filled world-space rectangle draw command.
   *
   * @param fill fill command to enqueue
   */
  public synchronized void addWorldFill(WorldFill fill) {
    worldFills.add(fill);
  }

  /**
   * Adds a screen-space text draw command.
   *
   * @param text text command to enqueue
   */
  public synchronized void addScreenText(ScreenText text) {
    screenTexts.add(text);
  }

  /**
   * Adds a screen-space marker draw command.
   *
   * @param marker marker command to enqueue
   */
  public synchronized void addScreenMarker(ScreenMarker marker) {
    screenMarkers.add(marker);
  }

  /**
   * Adds a world-space line draw command.
   *
   * @param line line command to enqueue
   */
  public synchronized void addWorldLine(WorldLine line) {
    worldLines.add(line);
  }

  /**
   * Adds a world-space circle outline draw command.
   *
   * @param circle circle outline command to enqueue
   */
  public synchronized void addWorldCircleOutline(WorldCircleOutline circle) {
    worldCircleOutlines.add(circle);
  }

  /**
   * Adds a filled world-space circle draw command.
   *
   * @param circle circle fill command to enqueue
   */
  public synchronized void addWorldCircleFill(WorldCircleFill circle) {
    worldCircleFills.add(circle);
  }

  /**
   * Adds a screen-space rectangle draw command.
   *
   * @param rectangle rectangle command to enqueue
   */
  public synchronized void addScreenRectangle(ScreenRectangle rectangle) {
    screenRectangles.add(rectangle);
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

  /** Removes all queued draw commands. */
  public synchronized void clear() {
    worldRectangles.clear();
    worldFills.clear();
    screenTexts.clear();
    screenMarkers.clear();
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
      && worldFills.isEmpty()
      && screenTexts.isEmpty()
      && screenMarkers.isEmpty()
      && worldLines.isEmpty()
      && worldCircleOutlines.isEmpty()
      && worldCircleFills.isEmpty()
      && screenRectangles.isEmpty();
  }
}
