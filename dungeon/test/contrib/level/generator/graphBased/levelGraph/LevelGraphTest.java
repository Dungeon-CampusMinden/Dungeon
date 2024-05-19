package contrib.level.generator.graphBased.levelGraph;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import core.Entity;
import java.util.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/** WTF? . */
public class LevelGraphTest {

  private LevelGraph graph;

  /** WTF? . */
  @Before
  public void setup() {
    // create a graph with 5 nodes where each node has 4 Neighbours
    graph = generateFullGraph();
  }

  /** WTF? . */
  @Test
  public void adapter_node_onNode() {
    // add new node
    graph.add(Set.of(new Entity()));

    // for manual check
    // System.out.println(graph.toDot());

    // 5 + adapter + new node
    assertEquals(7, graph.nodes().size());
  }

  /** WTF? . */
  @Test
  public void adapter_node_onTwoFullGraphs() {
    LevelGraph graph2 = generateFullGraph();
    LevelGraph.add(graph2, graph);
    // for manual check
    // System.out.println(graph.toDot());
    assertTrue(checkIfReachable(graph.root(), graph, graph2));
  }

  /** WTF? . */
  @Test
  public void adapter_node_onThreeFullGraphs() {
    LevelGraph graph2 = generateFullGraph();
    LevelGraph graph3 = generateFullGraph();
    LevelGraph.add(graph2, graph);
    LevelGraph.add(graph3, graph2);
    // for manual check
    // System.out.println(graph.toDot());
    assertTrue(checkIfReachable(graph.root(), graph, graph2, graph3));
  }

  /** WTF? . */
  @Test
  public void adapter_node_onThreeFullGraphs_connectOnOrigin() {
    LevelGraph graph2 = generateFullGraph();
    LevelGraph graph3 = generateFullGraph();
    LevelGraph.add(graph2, graph);
    LevelGraph.add(graph3, graph);
    // for manual check
    System.out.println(graph.toDot());
    assertTrue(checkIfReachable(graph.root(), graph, graph2, graph3));
  }

  /** WTF? . */
  @Test
  @Ignore
  public void connect_graphs_avoid_random_success() {
    // use this to check manual to avoid random success.
    List<LevelGraph> graphs = new ArrayList<>();
    for (int i = 10000; i < 0; i++) {
      LevelGraph g = generateFullGraph();
      graphs.add(g);
      if (i == 0) LevelGraph.add(g, graph);
      else LevelGraph.add(g, graphs.get(i - 1));
    }
    graphs.add(graph);
    assertTrue(checkIfReachable(graph.root(), graphs));
  }

  /** WTF? . */
  @Test
  public void no_adapter_onNode() {
    LevelGraph g1 = new LevelGraph();
    g1.add(Set.of(new Entity()));
    g1.add(Set.of(new Entity()));
    // 1+1 root + new Graph;
    assertEquals(2, g1.nodes().size());
  }

  /** WTF? . */
  @Test
  public void no_adapter_onGraph() {
    LevelGraph g1 = new LevelGraph();
    LevelGraph g2 = new LevelGraph();
    g1.add(Set.of(new Entity()));
    g2.add(Set.of(new Entity()));
    LevelGraph.add(g2, g1);
    assertTrue(checkIfReachable(g1.root(), g1, g2));
    // for manual check
    // System.out.println(g1.toDot());
  }

  /**
   * Generates a graph with 5 nodes where each node has 5 neighbours.
   *
   * @return generated graph.
   */
  private LevelGraph generateFullGraph() {
    // create second graph
    LevelGraph levelgraph = new LevelGraph();
    LevelNode n1 = new LevelNode(Set.of(new Entity()), levelgraph);
    LevelNode n2 = new LevelNode(Set.of(new Entity()), levelgraph);
    LevelNode n3 = new LevelNode(Set.of(new Entity()), levelgraph);
    LevelNode n4 = new LevelNode(Set.of(new Entity()), levelgraph);
    LevelNode n5 = new LevelNode(Set.of(new Entity()), levelgraph);

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
    levelgraph.addNodesToNodeList(Set.of(n1, n2, n3, n4, n5));
    return levelgraph;
  }

  /**
   * Checks if each Node in the given Graphs can be reached from the given root node.
   *
   * <p>Basically checks if the Graphs are connected.
   *
   * @param root root node
   * @param connectedWith graphs that should be reachable
   * @return true if the graphs are reachable from root, false if not
   */
  private boolean checkIfReachable(LevelNode root, LevelGraph... connectedWith) {
    return checkIfReachable(root, Arrays.stream(connectedWith).toList());
  }

  /**
   * Checks if each Node in the given Graphs can be reached from the given root node.
   *
   * <p>Basically checks if the Graphs are connected.
   *
   * @param root root node
   * @param connectedWith graphs that should be reachable
   * @return true if the graphs are reachable from root, false if not
   */
  private boolean checkIfReachable(LevelNode root, Collection<LevelGraph> connectedWith) {
    Set<LevelNode> needToBeVisited = new HashSet<>();
    connectedWith.forEach(c -> needToBeVisited.addAll(c.nodes()));
    Set<LevelNode> visited = depthFirstSearch(root);
    needToBeVisited.removeAll(visited);
    return needToBeVisited.isEmpty();
  }

  private Set<LevelNode> depthFirstSearch(LevelNode rootNode) {
    Set<LevelNode> visitedNodes = new HashSet<>();
    depthFirstSearchRecursive(rootNode, visitedNodes);
    return visitedNodes;
  }

  private void depthFirstSearchRecursive(LevelNode currentNode, Set<LevelNode> visitedNodes) {
    visitedNodes.add(currentNode);
    for (Direction direction : Direction.values()) {
      LevelNode neighbor = currentNode.at(direction).orElse(null);
      if (neighbor != null && !visitedNodes.contains(neighbor)) {
        depthFirstSearchRecursive(neighbor, visitedNodes);
      }
    }
  }
}
