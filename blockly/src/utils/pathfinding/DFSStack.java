package utils.pathfinding;

import core.level.utils.Coordinate;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A stack-based implementation of the GraphSearchDataStructure interface. This implementation uses
 * a LIFO (Last In, First Out) strategy, suitable for DFS (Depth-First Search).
 */
public class DFSStack implements GraphSearchDataStructure<Coordinate> {
  private final Deque<Coordinate> stack = new ArrayDeque<>();

  /**
   * Adds a coordinate to the top of the stack.
   *
   * @param coord The coordinate to add.
   */
  @Override
  public void push(Coordinate coord) {
    stack.push(coord);
  }

  /**
   * Removes and returns the coordinate at the top of the stack.
   *
   * @return The coordinate at the top of the stack, or null if the stack is empty.
   */
  @Override
  public Coordinate pop() {
    return stack.pop();
  }

  /**
   * Checks if the stack is empty.
   *
   * @return True if the stack is empty, false otherwise.
   */
  @Override
  public boolean isEmpty() {
    return stack.isEmpty();
  }

  /**
   * Returns whether the given element is present in the stack.
   *
   * @param element The element to check for.
   * @return True if the element is present, false otherwise.
   */
  @Override
  public boolean contains(Coordinate element) {
    return stack.contains(element);
  }
}
