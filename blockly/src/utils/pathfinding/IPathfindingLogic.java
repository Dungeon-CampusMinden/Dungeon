package utils.pathfinding;

import core.level.utils.Coordinate;
import java.util.List;
import java.util.Set;

/**
 * Interface defining the logic for pathfinding algorithms.
 *
 * @see systems.PathfindingSystem PathfindingSystem
 */
public interface IPathfindingLogic {

  /**
   * Initializes the pathfinding process with a start and end coordinate.
   *
   * @param start The starting coordinate of the pathfinding process.
   * @param end The target coordinate of the pathfinding process.
   */
  void initialize(Coordinate start, Coordinate end);

  /** Performs a single step in the pathfinding process. */
  void performStep();

  /**
   * Checks if the pathfinding search has finished.
   *
   * @return true if the search is complete, false otherwise.
   */
  boolean isSearchFinished();

  /**
   * Retrieves the set of coordinates currently in the open set. The open set contains nodes that
   * are yet to be processed.
   *
   * @return A set of coordinates in the open set.
   */
  Set<Coordinate> openSetCoordinates();

  /**
   * Retrieves the set of coordinates currently in the closed set. The closed set contains nodes
   * that have already been processed.
   *
   * @return A set of coordinates in the closed set.
   */
  Set<Coordinate> closedSetCoordinates();

  /**
   * Retrieves the last node processed during the pathfinding process.
   *
   * @return The coordinate of the last processed node.
   */
  Coordinate lastProcessedNode();

  /**
   * Retrieves the final path from the start to the end coordinate. This is typically called after
   * the search is finished.
   *
   * @return A list of coordinates representing the final path.
   */
  List<Coordinate> finalPath();
}
