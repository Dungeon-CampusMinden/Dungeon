package dojo.tasks;

import core.utils.IVoidFunction;
import dojo.rooms.TaskRoom;
import java.util.function.Function;

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

  public void question() {
    if (isActivated()) {
      questionOnActivated.execute();
    } else {
      questionOnDeactivated.execute();
      setActivated(true);
    }
  }

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

  public String getTaskName() {
    return taskName;
  }

  public boolean isCompleted() {
    return isCompleted;
  }

  public void setCompleted(boolean isComplete) {
    this.isCompleted = isComplete;
  }

  public boolean isActivated() {
    return isActivated;
  }

  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
  }

  public Task setShouldOpenDoors(boolean shouldOpenDoors) {
    this.shouldOpenDoors = shouldOpenDoors;
    return this;
  }
}
