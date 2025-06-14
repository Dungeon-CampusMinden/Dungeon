package aiAdvanced.pathfinding;

import core.level.utils.Coordinate;
import java.util.ArrayDeque;

/**
 * A stack-based implementation of the {@link ArrayDeque} for depth-first search (DFS).
 *
 * <p>This implementation uses {@link Coordinate} objects to represent nodes in the search space and
 * follows a LIFO (Last In, First Out) strategy, suitable for DFS.
 */
public class DFSStack extends ArrayDeque<Coordinate> {
  /**
   * Adds a coordinate to the top of the stack.
   *
   * @param coord The coordinate to add.
   */
  @Override
  public void push(Coordinate coord) {
    this.addLast(coord);
  }

  /**
   * Removes and returns the coordinate at the top of the stack.
   *
   * @return The coordinate at the top of the stack.
   */
  @Override
  public Coordinate pop() {
    if (isEmpty()) {
      return null;
    }
    return this.removeLast();
  }
}
