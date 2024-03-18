package tasks;

import core.utils.IVoidFunction;
import java.util.function.Function;

public class Task {
  private final TaskRoomGenerator trGen;
  private final String taskName;
  private final IVoidFunction questionOnActivated;
  private final IVoidFunction questionOnDeactivated;
  private final Function<Task, Boolean> solveOnActivated;
  private final IVoidFunction solveOnDeactivated;
  private boolean isActivated = false;
  private boolean isCompleted = false;

  public Task(
      TaskRoomGenerator trGen,
      String taskName,
      IVoidFunction questionOnActivated,
      IVoidFunction questionOnDeactivated,
      Function<Task, Boolean> solveOnActivated,
      IVoidFunction solveOnDeactivated) {
    this.trGen = trGen;
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
        if (trGen.areAllTasksCompleted()) {
          trGen.openDoors();
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
}
