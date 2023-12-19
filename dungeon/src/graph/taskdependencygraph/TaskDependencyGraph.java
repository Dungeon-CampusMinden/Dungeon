package graph.taskdependencygraph;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A Graph consists of nodes, each representing a task, and edges represent the dependencies between
 * the tasks.
 *
 * <p>The level graph and Petri net are generated from the dependency graph.
 *
 * <p>This class is used to store information, not to create the graph itself. Therefore, create the
 * {@link TaskNode}s and {@link TaskEdge}s separately and pass the collections containing all nodes
 * and edges here in the constructor.
 *
 * <p>The graph does not need to be connected.
 *
 * <p>You can obtain an iterator over all edges using {@link #edgeIterator()}.
 *
 * <p>You can obtain an iterator over all nodes using {@link #nodeIterator()}.
 */
public class TaskDependencyGraph {
  private final ArrayList<TaskEdge> edges;
  private final ArrayList<TaskNode> nodes;

  /**
   * Create a TaskDependencyGraph.
   *
   * @param edges All edges in the graph.
   * @param nodes All nodes in the graph.
   */
  public TaskDependencyGraph(ArrayList<TaskEdge> edges, ArrayList<TaskNode> nodes) {
    this.edges = edges;
    this.nodes = nodes;
  }

  /**
   * Returns an iterator that can be used to traverse through all the edges in the graph.
   *
   * @return An iterator over all edges in the graph.
   */
  public Iterator<TaskEdge> edgeIterator() {
    return edges.iterator();
  }

  /**
   * Returns an iterator that can be used to traverse through all the nodes in the graph.
   *
   * @return An iterator over all nodes in the graph.
   */
  public Iterator<TaskNode> nodeIterator() {
    return nodes.iterator();
  }
}
