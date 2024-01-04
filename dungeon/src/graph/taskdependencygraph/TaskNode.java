package graph.taskdependencygraph;

import task.Task;

/**
 * Represents a node in a {@link TaskDependencyGraph} that stores a {@link Task}.
 *
 * <p>Each node is associated with a unique index and a task.
 */
public class TaskNode implements Comparable<TaskNode> {
  /** A special TaskNode representing none or no task. */
  public static TaskNode NONE = new TaskNode(null);

  private static int _idx;
  private final int idx;
  private final Task task;

  /**
   * Constructor to create a TaskNode.
   *
   * @param task The task to store in the node.
   */
  public TaskNode(Task task) {
    this.idx = _idx++;
    this.task = task;
  }

  /**
   * Get the unique index of this node.
   *
   * @return The unique index of this node.
   */
  public int getIdx() {
    return idx;
  }

  /**
   * Get the task stored in this node.
   *
   * @return The task stored in this node.
   */
  public Task task() {
    return this.task;
  }

  /**
   * Compares this TaskNode to another based on their indices.
   *
   * @param o The TaskNode to compare to.
   * @return a negative integer, zero, or a positive integer as this TaskNode is less than, equal
   *     to, or greater than the specified TaskNode.
   */
  @Override
  public int compareTo(TaskNode o) {
    return this.idx - o.idx;
  }
}
