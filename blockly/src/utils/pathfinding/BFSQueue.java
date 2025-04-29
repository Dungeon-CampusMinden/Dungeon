package utils.pathfinding;

import core.level.utils.Coordinate;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A queue-based implementation of the GraphSearchDataStructure interface.
 *
 * <p>This implementation uses a FIFO (First In, First Out) strategy, suitable for BFS
 * (Breadth-First Search).
 */
public class BFSQueue implements GraphSearchDataStructure<Coordinate> {
  private final Deque<Coordinate> queue = new ArrayDeque<>();

  /**
   * Adds a coordinate to the end of the queue.
   *
   * @param coord The coordinate to add.
   */
  @Override
  public void push(Coordinate coord) {
    queue.addLast(coord);
  }

  /**
   * Removes and returns the coordinate at the front of the queue.
   *
   * @return The coordinate at the front of the queue, or null if the queue is empty.
   */
  @Override
  public Coordinate pop() {
    return queue.pollFirst();
  }

  /**
   * Checks if the queue is empty.
   *
   * @return True if the queue is empty, false otherwise.
   */
  @Override
  public boolean isEmpty() {
    return queue.isEmpty();
  }

  /**
   * Returns whether the given element is present in the queue.
   *
   * @param element The element to check for.
   * @return True if the element is present, false otherwise.
   */
  @Override
  public boolean contains(Coordinate element) {
    return queue.contains(element);
  }
}
