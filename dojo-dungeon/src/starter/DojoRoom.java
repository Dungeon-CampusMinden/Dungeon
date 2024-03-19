package starter;

import contrib.level.generator.GeneratorUtils;
import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import core.level.elements.tile.DoorTile;
import java.util.Set;

/**
 * This class holds one level node and its entities.
 *
 * <p>This class is useful to build a level graph and to add later entities to it.
 */
public class DojoRoom extends LevelNode {
  private DojoRoom nextRoom;

  /**
   * Creates a new room and initializes its container with an empty collection as payload.
   *
   * @param originGraph is the graph in which this node was initially created and added. It helps to
   *     differentiate nodes in connected graphs.
   */
  public DojoRoom(final LevelGraph originGraph) {
    super(originGraph);
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
    super.connect(other, direction);
  }

  /**
   * Sets the next room. The next room can be {@code null} in case there is no next room. The next
   * room is mainly used to close or open the doors within the rooms.
   *
   * @param nextRoom the next room to set
   */
  public void setNextRoom(DojoRoom nextRoom) {
    this.nextRoom = nextRoom;
  }

  /** Method to open doors between this room and next room. */
  public void openDoors() {
    if (nextRoom == null) {
      return;
    }
    DoorTile door12 = GeneratorUtils.doorAt(this.level(), Direction.SOUTH).orElseThrow();
    door12.open();
    DoorTile door21 = GeneratorUtils.doorAt(nextRoom.level(), Direction.NORTH).orElseThrow();
    door21.open();
  }

  /** Method to close doors between this room and next room. */
  public void closeDoors() {
    if (nextRoom == null) {
      return;
    }
    GeneratorUtils.doorAt(this.level(), Direction.SOUTH).orElseThrow().close();
    GeneratorUtils.doorAt(nextRoom.level(), Direction.NORTH).orElseThrow().close();
  }
}
