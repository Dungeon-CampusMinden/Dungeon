package tasks;

import core.utils.IVoidFunction;
import java.util.function.Function;

public class Task {
  private final TaskRoomGenerator trGen;
  private final IVoidFunction questionOnActivated;
  private final IVoidFunction questionOnDeactivated;
  private final Function<Task, Boolean> solveOnActivated;
  private final IVoidFunction solveOnDeactivated;
  private boolean isActivated = false;
  private boolean completed = false;

  public Task(
      TaskRoomGenerator trGen,
      IVoidFunction questionOnActivated,
      IVoidFunction questionOnDeactivated,
      Function<Task, Boolean> solveOnActivated,
      IVoidFunction solveOnDeactivated) {
    this.trGen = trGen;
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

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public boolean isActivated() {
    return isActivated;
  }

  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
  }
}
