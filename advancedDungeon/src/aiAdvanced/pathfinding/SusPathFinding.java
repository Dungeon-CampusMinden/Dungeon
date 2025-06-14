package aiAdvanced.pathfinding;

import core.level.utils.Coordinate;

/**
 * {@code SusPathFinding} is a customizable pathfinding class intended for student implementation.
 *
 * <p>It extends {@link PathfindingLogic} and allows learners to experiment with different search
 * strategies by choosing between a {@link BFSQueue} (FIFO) or {@link DFSStack} (LIFO) as the
 * frontier data structure.
 *
 * <p>This class serves as a template for recreating common pathfinding algorithms such as BFS or
 * DFS.
 *
 * <p>Students are expected to:
 *
 * <ul>
 *   <li>Choose the desired frontier data structure in the constructor.
 *   <li>Implement the {@link #performSearch()} method with their own algorithm.
 * </ul>
 *
 * @see PathfindingLogic
 * @see aiAdvanced.systems.PathfindingSystem
 * @see BFSQueue
 * @see DFSStack
 */
public class SusPathFinding extends PathfindingLogic {
  /**
   * Constructs a new {@code SusPathFinding} instance.
   *
   * <p>This constructor initializes the pathfinding algorithm with a chosen data structure for the
   * frontier set. Students should select either a {@link BFSQueue} (for breadth-first search
   * behavior) or a {@link DFSStack} (for depth-first search).
   *
   * <p>TODO: Replace {@code BFSQueue} with the desired data structure (e.g., {@code DFSStack}).
   *
   * @param start The starting coordinate for the pathfinding search.
   * @param end The ending coordinate for the pathfinding search.
   */
  public SusPathFinding(Coordinate start, Coordinate end) {
    super(start, end, new BFSQueue()); // TODO: Replace with DFSStack if needed
  }

  /**
   * Performs the pathfinding search.
   *
   * <p>This method must be implemented by students to define their own search logic.
   *
   * <p>TODO: Implement the pathfinding logic.
   *
   * @throws UnsupportedOperationException if not yet implemented
   */
  @Override
  public void performSearch() {
    throw new UnsupportedOperationException("Bitte eigenes Pathfinding implementieren.");
  }
}
