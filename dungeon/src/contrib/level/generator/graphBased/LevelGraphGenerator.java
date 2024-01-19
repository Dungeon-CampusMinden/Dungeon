package contrib.level.generator.graphBased;

import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import java.util.HashSet;
import java.util.Set;

/**
 * Generates a {@link LevelGraph} that can be further processed into a room-based level.
 *
 * <p>This generator is specifically designed to work in conjunction with the DSL concept and is
 * provided with a collection of entity collections. For each collection, a node in the graph is
 * created, and the collection is set as payload. The collection can be queried using {@link
 * LevelNode#entities()}.
 *
 * <p>The generator first generates a tree and then adds random edges to the tree to create a
 * diverse graph.
 *
 * <p>Each node can have a maximum of 4 neighbors, and each edge is oriented towards a {@link
 * Direction} within the node. The {@link Direction} indicates the side of the room where the door
 * should be placed. The edges are oriented so that a west edge from node A to B corresponds to an
 * east edge from B to A.
 *
 * <p>Edges are unidirectional.
 *
 * <p>There is no separate data type for edges; instead, the {@link LevelNode}s store an array of
 * neighboring nodes, and the index in the array indicates the {@link Direction} through which the
 * nodes are connected.
 */
public final class LevelGraphGenerator {

  /**
   * Generates a {@link LevelGraph} with one node for each provided collection of entities.
   *
   * @param entityCollections A collection of entity collections. For each entity collection, a node
   *     will be created, and the entity collection will be added as payload.
   * @return The generated graph.
   */
  public static LevelGraph generate(final Set<Set<Entity>> entityCollections) {
    LevelGraph graph = new LevelGraph();
    // this will generate a tree
    entityCollections.forEach(graph::add);
    // draw some random edges to make it more fun
    // TODO add some more rules so the level graph are more fun and less confusing
    // graph.addRandomEdges(RANGE_OF_RANDOM_EDGE_COUNT);
    return graph;
  }

  /**
   * Generates a {@link LevelGraph} with the given numbers of nodes.
   *
   * @param nodeCount Number of nodes in the graph.
   * @return The generated graph.
   */
  public static LevelGraph generate(int nodeCount) {
    Set<Set<Entity>> outerSet = new HashSet<>();
    for (int i = 0; i < nodeCount; i++) outerSet.add(Set.of(new Entity()));
    return generate(outerSet);
  }
}
