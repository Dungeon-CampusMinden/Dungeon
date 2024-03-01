package task;

import dsl.annotation.DSLType;
import task.game.components.TaskContentComponent;

/**
 * A part of the content of a complete task.
 *
 * <p>TaskContent objects are represented by the {@link TaskContentComponent} of entities in the
 * game.
 *
 * <p>A TaskContent can, for example, be an answer option for a quiz question or a rule for
 * replacement tasks.
 */
@DSLType
public abstract class TaskContent {
  private Task task;

  /**
   * Creates a new TaskContent.
   *
   * <p>This Content will not automatically register itself at the Task. Call {@link
   * Task#addContent(TaskContent)} for that.
   *
   * @param task Task to which this content belongs.
   */
  public TaskContent(final Task task) {
    this.task = task;
  }

  /** Create a new TaskContent without a task reference. */
  public TaskContent() {}

  /**
   * Return associated Task.
   *
   * @return task to which this content belongs.
   */
  public Task task() {
    return task;
  }

  /**
   * Set the reference to the task if no reference is set yet.
   *
   * <p>If the reference was set, this Content will not automatically register itself at the Task.
   * Call {@link Task#addContent(TaskContent)} for that.
   *
   * <p>Note: You cannot change the reference because then this content must be removed from the
   * Task, and at the moment there is no functionality for that because we don't think we will need
   * it.
   *
   * @param task Task to set the reference to.
   * @return true if the reference was set, false if not (this happens if a reference is already
   *     set).
   */
  public boolean task(Task task) {
    if (this.task == null) {
      this.task = task;
      return true;
    }
    return false;
  }
}
