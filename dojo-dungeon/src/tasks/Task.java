package tasks;

import java.util.function.Consumer;
import java.util.function.Function;

public class Task {
  private final TaskRoomGenerator trGen;
  private final Consumer<Task> questionOnActivated;
  private final Consumer<Task> questionOnDeactivated;
  private final Function<Task, Boolean> solveOnActivated;
  private final Consumer<Task> solveOnDeactivated;
  private boolean isActivated = false;
  private boolean completed = false;

  public Task(
      TaskRoomGenerator trGen,
      Consumer<Task> questionOnActivated,
      Consumer<Task> questionOnDeactivated,
      Function<Task, Boolean> solveOnActivated,
      Consumer<Task> solveOnDeactivated) {
    this.trGen = trGen;
    this.questionOnActivated = questionOnActivated;
    this.questionOnDeactivated = questionOnDeactivated;
    this.solveOnActivated = solveOnActivated;
    this.solveOnDeactivated = solveOnDeactivated;
  }

  public void question() {
    if (isActivated()) {
      questionOnActivated.accept(this);
    } else {
      questionOnDeactivated.accept(this);
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
      solveOnDeactivated.accept(this);
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
