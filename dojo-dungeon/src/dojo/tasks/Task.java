package dojo.tasks;

import core.utils.IVoidFunction;
import dojo.rooms.TaskRoom;
import java.util.function.Function;

/** Class for holding information about a task. */
public class Task {
  private final TaskRoom taskRoom;
  private final String taskName;
  private final IVoidFunction questionOnActivated;
  private final IVoidFunction questionOnDeactivated;
  private final Function<Task, Boolean> solveOnActivated;
  private final IVoidFunction solveOnDeactivated;
  private boolean isActivated = false;
  private boolean isCompleted = false;
  private boolean shouldOpenDoors = true;

  /**
   * Creates a new task with its callbacks and data.
   *
   * @param taskRoom the room of this task
   * @param taskName the name of this task
   * @param questionOnActivated callback to execute when the task is activated
   * @param questionOnDeactivated callback to execute when the task is deactivated
   * @param solveOnActivated callback to execute when the task is activated and solved
   * @param solveOnDeactivated callback to execute when the task is deactivated and solved
   */
  public Task(
      TaskRoom taskRoom,
      String taskName,
      IVoidFunction questionOnActivated,
      IVoidFunction questionOnDeactivated,
      Function<Task, Boolean> solveOnActivated,
      IVoidFunction solveOnDeactivated) {
    this.taskRoom = taskRoom;
    this.taskName = taskName;
    this.questionOnActivated = questionOnActivated;
    this.questionOnDeactivated = questionOnDeactivated;
    this.solveOnActivated = solveOnActivated;
    this.solveOnDeactivated = solveOnDeactivated;
  }

  /** Method to execute for tell the question. */
  public void question() {
    if (isActivated()) {
      questionOnActivated.execute();
    } else {
      questionOnDeactivated.execute();
      setActivated(true);
    }
  }

  /** Method to execute for solve the task. */
  public void solve() {
    if (isActivated()) {
      if (solveOnActivated.apply(this)) {
        setCompleted(true);
        if (shouldOpenDoors && taskRoom.areAllTasksCompleted()) {
          taskRoom.openDoors();
        }
      }
    } else {
      solveOnDeactivated.execute();
    }
  }

  /**
   * Return the name of this task.
   *
   * @return the name of this task.
   */
  public String getTaskName() {
    return taskName;
  }

  /**
   * Return if this task is completed or not.
   *
   * @return true if this task is completed.
   */
  public boolean isCompleted() {
    return isCompleted;
  }

  /**
   * Set if the task is completed or not.
   *
   * @param isComplete the value to set.
   */
  public void setCompleted(boolean isComplete) {
    this.isCompleted = isComplete;
  }

  /**
   * If this task is activated or not.
   *
   * @return true if the task is activated.
   */
  public boolean isActivated() {
    return isActivated;
  }

  /**
   * Set if the task is activated or not.
   *
   * @param isActivated the value to set.
   */
  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
  }

  /**
   * Set if this task should open the door if all tasks are completed.
   *
   * @param shouldOpenDoors true if the task should open the door.
   * @return the current task object.
   */
  public Task setShouldOpenDoors(boolean shouldOpenDoors) {
    this.shouldOpenDoors = shouldOpenDoors;
    return this;
  }
}
