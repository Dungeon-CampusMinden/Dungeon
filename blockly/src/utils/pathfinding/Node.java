package utils.pathfinding;

import core.level.utils.Coordinate;
import java.util.List;
import utils.LevelUtils;

/**
 * A Node represents a point in the pathfinding grid.
 *
 * <p>Each node has a coordinate and a list of its walkable neighbors. This class is used in
 * pathfinding algorithms to represent the state of the search.
 *
 * <p>It provides methods to access the coordinate and neighbors of the node.
 *
 * @see PathfindingLogic
 */
public class Node {
  private final Coordinate coordinate;
  private final List<Coordinate> neighbors;

  /**
   * Creates a new Node with the specified coordinate.
   *
   * @param coordinate The coordinate of the node.
   */
  public Node(Coordinate coordinate) {
    this.coordinate = coordinate;
    neighbors = LevelUtils.walkableNeighbors(coordinate);
  }

  /**
   * Returns the coordinate of the node.
   *
   * @return The coordinate of the node.
   */
  public Coordinate coordinate() {
    return coordinate;
  }

  /**
   * Returns the list of walkable neighbors of the node.
   *
   * @return The list of walkable neighbors.
   */
  public List<Coordinate> neighbors() {
    return neighbors;
  }

  @Override
  public String toString() {
    return "Node{" + "coordinate=" + coordinate + ", neighbors=" + neighbors + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Node node = (Node) obj;
    return coordinate.equals(node.coordinate);
  }
}
