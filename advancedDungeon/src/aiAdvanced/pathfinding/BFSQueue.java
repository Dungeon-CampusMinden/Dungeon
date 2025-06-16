package aiAdvanced.pathfinding;

import core.level.utils.Coordinate;
import java.util.ArrayDeque;

/**
 * A queue-based implementation of the {@link ArrayDeque} for breadth-first search (BFS).
 *
 * <p>This implementation uses {@link Coordinate} objects to represent nodes in the search space and
 * follows a FIFO (First In, First Out) strategy, suitable for BFS.
 */
public class BFSQueue extends ArrayDeque<Coordinate> {
  /**
   * Adds a coordinate to the end of the queue.
   *
   * @param coord The coordinate to add.
   */
  @Override
  public void push(Coordinate coord) {
    this.addLast(coord);
  }

  /**
   * Removes and returns the coordinate at the front of the queue.
   *
   * @return The coordinate at the front of the queue, or null if the queue is empty.
   */
  @Override
  public Coordinate pop() {
    if (isEmpty()) {
      return null;
    }
    return this.removeFirst();
  }
}
