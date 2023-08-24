package core.level.generator.graphBased.graph;

import core.Entity;

import java.util.Random;
import java.util.Set;

/**
 * Generates a {@link LevelGraph} that can be further processed into a room-based level.
 *
 * <p>This generator is specifically designed to work in conjunction with the DSL concept and is
 * provided with a collection of entity collections. For each collection, a node in the graph is
 * created, and the collection is set as payload. The collection can be queried using {@link
 * LevelGraph.Node#entities()}.
 *
 * <p>The generator first generates a tree and then adds random edges to the tree to create a
 * diverse graph.
 *
 * <p>Each node can have a maximum of 4 neighbors, and each edge is oriented towards a {@link
 * LevelGraph.Direction} within the node. The {@link LevelGraph.Direction} indicates the side of the
 * room where the door should be placed. The edges are oriented so that a west edge from node A to B
 * corresponds to an east edge from B to A.
 *
 * <p>Edges are unidirectional.
 *
 * <p>There is no separate data type for edges; instead, the {@link LevelGraph.Node}s store an array
 * of neighboring nodes, and the index in the array indicates the {@link LevelGraph.Direction}
 * through which the nodes are connected.
 */
public class GraphGenerator {
    private static final Random RANDOM = new Random();
    private static final int RANGE_OF_RANDOM_EDGE_COUNT = 3;

    /**
     * Generates a {@link LevelGraph} with one node for each provided collection of entities.
     *
     * @param entityCollections A collection of entity collections. For each entity collection, a
     *     node will be created, and the entity collection will be added as payload.
     * @return The generated graph.
     */
    public static LevelGraph generate(Set<Set<Entity>> entityCollections) {
        LevelGraph graph = new LevelGraph();
        // this will generate a tree
        entityCollections.forEach(graph::add);
        // draw some random edges to make it more fun
        addRandomEdges(entityCollections, graph);
        return graph;
    }

    private static void addRandomEdges(Set<Set<Entity>> entityCollections, LevelGraph graph) {
        int nodeCount = entityCollections.size();
        // for two nodes no extra edges are needed
        if (nodeCount >= 3) {
            int howManyExtraEdges =
                    RANDOM.nextInt(nodeCount / RANGE_OF_RANDOM_EDGE_COUNT, nodeCount);

            for (int i = 0; i < howManyExtraEdges; i++) {
                LevelGraph.Node a = graph.random();
                LevelGraph.Node b = graph.random();
                if (a == b || a.isNeighbourWith(b)) i = i - 1;
                else {
                    a.addAtRandomDirection(b);
                }
            }
        }
    }
}
