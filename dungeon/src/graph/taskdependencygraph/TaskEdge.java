package graph.taskdependencygraph;

/**
 * Represents an edge in a {@link TaskDependencyGraph} that connects two {@link TaskNode}s.
 *
 * <p>The {@link Type} describes the type of dependency between the start and end nodes.
 *
 * <p>Edges can be considered bidirectional, so do not create two edge instances for both directions
 * (from 'a' to 'b' and from 'b' to 'a').
 */
public class TaskEdge implements Comparable<TaskEdge> {
  private static int _idx;
  private final int idx;
  private final Type edgeType;
  private final TaskNode startNode;
  private final TaskNode endNode;

  /**
   * Constructor to create a TaskEdge.
   *
   * @param edgeType The type of the edge (e.g., subtask_mandatory, subtask_optional, sequence,
   *     etc.)
   * @param startNode The node at the beginning of the edge.
   * @param endNode The node at the end of the edge.
   */
  public TaskEdge(Type edgeType, TaskNode startNode, TaskNode endNode) {
    this.idx = _idx++;
    this.edgeType = edgeType;
    this.startNode = startNode;
    this.endNode = endNode;
  }

  /**
   * Get the unique index of this edge.
   *
   * @return The unique index of this edge.
   */
  public int getIdx() {
    return idx;
  }

  /**
   * Get the {@link Type} of this edge.
   *
   * @return The type of this edge.
   */
  public Type edgeType() {
    return edgeType;
  }

  /**
   * Get the start node of this edge.
   *
   * @return The start node of this edge.
   */
  public TaskNode startNode() {
    return startNode;
  }

  /**
   * Get the end node of this edge.
   *
   * @return The end node of this edge.
   */
  public TaskNode endNode() {
    return endNode;
  }

  /**
   * Get a formatted name for this edge, featuring the values and hashCodes for each node (for
   * identification) and a marker for the edgeType.
   *
   * @return A formatted name for this edge.
   */
  public String name() {
    String separator = "->";
    return String.format(
        "%1$s[%2$s] %3$s[%4$s] %5$s[%6$s]",
        startNode.task(),
        startNode.hashCode(),
        separator,
        edgeType,
        endNode.task(),
        endNode.hashCode());
  }

  /**
   * Compares this TaskEdge to another based on their indices.
   *
   * @param o The TaskEdge to compare to.
   * @return a negative integer, zero, or a positive integer as this TaskEdge is less than, equal
   *     to, or greater than the specified TaskEdge.
   */
  @Override
  public int compareTo(TaskEdge o) {
    return this.idx - o.idx;
  }

  /** Enumeration representing the types of edges that can exist in the graph. */
  public enum Type {
    /** The edge is a mandatory subtask. */
    subtask_mandatory,
    /** The edge is an optional subtask. */
    subtask_optional,
    /** The edge is a sequence. */
    sequence,
    /** The edge is a sequence and. */
    sequence_and,
    /** The edge is a sequence or. */
    sequence_or,
    /** The edge is a conditional. */
    conditional_false,
    /** The edge is a conditional. */
    conditional_correct
  }
}
