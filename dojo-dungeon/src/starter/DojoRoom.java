package starter;

import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import java.util.HashSet;
import java.util.Set;

/**
 * A room of the dojo-dungeon game. This class holds one level node and its entities.
 *
 * <p>This class is useful to build a level graph and to add later entities to it.
 */
public class DojoRoom {
  private final Set<Entity> entities = new HashSet<>();
  private final LevelNode root;

  /**
   * Creates a new room and initializes its container with an empty collection as payload.
   *
   * @param originGraph is the graph in which this node was initially created and added. It helps to
   *     differentiate nodes in connected graphs.
   */
  public DojoRoom(final LevelGraph originGraph) {
    root = new LevelNode(entities, originGraph);
  }

  /**
   * Adds an entity to the set of entities.
   *
   * @param entity the entity to add
   */
  public void addEntity(final Entity entity) {
    entities.add(entity);
  }

  /**
   * Add entities to the existing set of entities.
   *
   * @param entities the set of entities to add
   */
  public void addEntities(final Set<Entity> entities) {
    this.entities.addAll(entities);
  }

  /**
   * Get the containing level node.
   *
   * @return the containing level node
   */
  public LevelNode getLevelNode() {
    return root;
  }

  /**
   * Adds a neighbor in the given direction.
   *
   * <p>If the origin graph of the given node is not the same graph as the origin graph of this
   * node, the graphs get connected. All nodes from this origin graph will be added to the given
   * node's origin graph, and vice versa.
   *
   * <p>This method only establishes the connection from this node to the other. Remember to also
   * call this function for the given node with the opposite direction to complete the connection.
   *
   * @param other The neighbor to be added.
   * @param direction The direction at which the neighbor should be added from this node's
   *     perspective (in the neighbor's context, this corresponds to the opposite direction).
   */
  public void connect(final DojoRoom other, final Direction direction) {
    root.connect(other.root, direction);
  }
}
