package dojo.rooms;

import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import core.Game;
import java.util.Set;

/**
 * This class holds one level node and its entities.
 *
 * <p>This class is needed to build a level graph, and to add entities at runtime in the game.
 *
 * <p>You can:
 *
 * <ul>
 *   <li>Create a {@link LevelNode} with the constructor.
 *   <li>Link two {@link LevelNode}s with the {@link LevelNode#connect(LevelNode, Direction)}
 *       method.
 * </ul>
 *
 * <p>You cannot generate a room with this class. This must be done in the {@link Room} class.
 */
public class LevelRoom extends LevelNode {

  /**
   * Constructor that takes a LevelGraph for all room nodes.
   *
   * @param originGraph the level graph for all room nodes.
   */
  public LevelRoom(LevelGraph originGraph) {
    super(originGraph);
  }

  /**
   * Add the entities as payload to the LevelNode.
   *
   * <p>This will add the entities (in the node payload) to the game, at the moment the level get
   * loaded for the first time.
   *
   * @param roomEntities the entities to add.
   */
  public void addRoomEntities(Set<Entity> roomEntities) {
    entities.addAll(roomEntities);

    level().onFirstLoad(() -> entities().forEach(Game::add));
  }

  /**
   * Adds an entity immediately to the set of entities.
   *
   * @param entity the entity to add.
   */
  public void addEntityImmediately(final Entity entity) {
    entities.add(entity);

    Game.add(entity);
  }
}
