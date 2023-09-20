package contrib.level.generator.graphBased.levelGraph;

import static junit.framework.TestCase.assertEquals;

import core.Entity;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class LevelGraphTest {

    private LevelGraph graph;

    @Before
    public void setup() {
        // create a graph with 5 nodes where each node has 4 Neighbours
        graph = generateFullGraph();
    }

    @Test
    public void adapter_node_onNode() {
        // add new node
        graph.add(Set.of(new Entity()));
        // 5 + adapter + new node
        assertEquals(7, graph.nodes().size());
        // for manual check
        // System.out.println(graph.toDot());
    }

    @Test
    public void adapter_node_onTwoFullGraphs() {
        LevelGraph graph2 = generateFullGraph();
        graph.add(graph2, graph);
        // 5+5 for each graph +2 adapter nodes
        assertEquals(12, graph.nodes().size());
        // for manual check
        // System.out.println(graph.toDot());
    }

    @Test
    public void adapter_node_onThreeFullGraphs() {
        LevelGraph graph2 = generateFullGraph();
        LevelGraph graph3 = generateFullGraph();
        graph.add(graph2, graph);
        graph.add(graph3, graph2);
        // 5+5+5 for each graph +4 adapter nodes
        assertEquals(19, graph.nodes().size());
        // for manual check
        System.out.println(graph.toDot());
    }

    @Test
    public void adapter_node_onThreeFullGraphs_connectOnOrigin() {
        LevelGraph graph2 = generateFullGraph();
        LevelGraph graph3 = generateFullGraph();
        graph.add(graph2, graph);
        graph.add(graph3, graph);
        // 5+5+5 for each graph +4 adapter nodes
        assertEquals(19, graph.nodes().size());
        // for manual check
        System.out.println(graph.toDot());
    }

    @Test
    public void no_adapter_onNode() {
        LevelGraph g1 = new LevelGraph();
        g1.add(Set.of(new Entity()));
        g1.add(Set.of(new Entity()));
        // 1+1 root + new Graph;
        assertEquals(2, g1.nodes().size());
    }

    @Test
    public void no_adapter_onGraph() {
        LevelGraph g1 = new LevelGraph();
        LevelGraph g2 = new LevelGraph();
        g1.add(Set.of(new Entity()));
        g2.add(Set.of(new Entity()));
        g1.add(g2, g1);
        // 1+1 for each graph, no adapter needed
        assertEquals(2, g1.nodes().size());
        // for manual check
        System.out.println(g1.toDot());
    }

    /**
     * Generates a graph with 5 nodes where each node has 5 neighbours
     *
     * @return generated graph
     */
    private LevelGraph generateFullGraph() {
        // create second graph
        LevelGraph levelgraph = new LevelGraph();
        Node n1 = new Node(Set.of(new Entity()), levelgraph);
        Node n2 = new Node(Set.of(new Entity()), levelgraph);
        Node n3 = new Node(Set.of(new Entity()), levelgraph);
        Node n4 = new Node(Set.of(new Entity()), levelgraph);
        Node n5 = new Node(Set.of(new Entity()), levelgraph);

        n1.forceNeighbor(n2, Direction.EAST);
        n2.forceNeighbor(n1, Direction.WEST);
        n1.forceNeighbor(n3, Direction.SOUTH);
        n3.forceNeighbor(n1, Direction.NORTH);
        n1.forceNeighbor(n4, Direction.WEST);
        n4.forceNeighbor(n1, Direction.EAST);
        n1.forceNeighbor(n5, Direction.NORTH);
        n5.forceNeighbor(n1, Direction.SOUTH);
        n2.forceNeighbor(n3, Direction.NORTH);
        n3.forceNeighbor(n2, Direction.SOUTH);
        n2.forceNeighbor(n4, Direction.SOUTH);
        n4.forceNeighbor(n2, Direction.NORTH);
        n2.forceNeighbor(n5, Direction.EAST);
        n5.forceNeighbor(n2, Direction.WEST);
        n3.forceNeighbor(n4, Direction.EAST);
        n4.forceNeighbor(n3, Direction.WEST);
        n3.forceNeighbor(n5, Direction.WEST);
        n5.forceNeighbor(n3, Direction.EAST);
        n4.forceNeighbor(n5, Direction.SOUTH);
        n5.forceNeighbor(n4, Direction.NORTH);
        levelgraph.addNodes(Set.of(n1, n2, n3, n4, n5));
        return levelgraph;
    }
}
