package utils.pathfinding;

import core.level.utils.Coordinate;
import java.util.*;
import utils.LevelUtils;

/**
 * Breadth-First Search (BFS) pathfinding algorithm implementation.
 *
 * <p>This class implements the IPathfindingLogic interface and provides the logic for performing
 * BFS pathfinding. It maintains the open and closed sets, tracks the path, and provides methods to
 * initialize the search, perform steps, and retrieve the final path.
 *
 * @see IPathfindingLogic
 * @see systems.PathfindingSystem PathfindingSystem
 */
public class BFSPathFinding implements IPathfindingLogic {
  private Queue<Coordinate> queue;
  private Set<Coordinate> openSet;
  private Set<Coordinate> closedSet;
  private Map<Coordinate, Coordinate> cameFrom;
  private Coordinate start;
  private Coordinate end;
  private Coordinate lastProcessed;
  private boolean isFinished;

  @Override
  public void initialize(Coordinate start, Coordinate end) {
    this.queue = new LinkedList<>();
    this.openSet = new HashSet<>();
    this.closedSet = new HashSet<>();
    this.cameFrom = new HashMap<>();
    this.start = start;
    this.end = end;
    this.lastProcessed = null;
    this.isFinished = false;

    // Add starting node to the queue and open set
    queue.add(start);
    openSet.add(start);
  }

  @Override
  public void performStep() {
    if (queue.isEmpty() || isFinished) {
      isFinished = true;
      return;
    }

    // Get the next node from the queue
    Coordinate current = queue.poll();
    lastProcessed = current;

    // Move current from an open set to closed set
    openSet.remove(current);
    closedSet.add(current);

    // Check if we've reached the end
    if (current.equals(end)) {
      isFinished = true;
      return;
    }

    // Get valid neighbors (4-way movement)
    List<Coordinate> neighbors = LevelUtils.walkableNeighbors(current);

    // Process each valid neighbor
    for (Coordinate neighbor : neighbors) {
      if (!openSet.contains(neighbor)) {
        // Add to queue and open set
        queue.add(neighbor);
        openSet.add(neighbor);
        // Record where we came from for path reconstruction
        cameFrom.put(neighbor, current);
      }
    }
  }

  @Override
  public boolean isSearchFinished() {
    return isFinished;
  }

  @Override
  public Set<Coordinate> openSetCoordinates() {
    return new HashSet<>(openSet);
  }

  @Override
  public Set<Coordinate> closedSetCoordinates() {
    return new HashSet<>(closedSet);
  }

  @Override
  public Coordinate lastProcessedNode() {
    return lastProcessed;
  }

  @Override
  public List<Coordinate> finalPath() {
    if (!isFinished || !closedSet.contains(end)) {
      return List.of(); // No path found
    }

    // Reconstruct path
    List<Coordinate> path = new ArrayList<>();
    Coordinate current = end;

    while (!current.equals(start)) {
      path.add(current);
      current = cameFrom.get(current);
    }

    path.add(start);

    return path;
  }
}
