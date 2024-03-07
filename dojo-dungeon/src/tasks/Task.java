package tasks;

import java.util.function.Function;
import starter.DojoStarter;

public class Task {
  private final TaskRoomGenerator trGen;
  private final Function<Task, Boolean> questionOnActivated;
  private final Function<Task, Boolean> questionOnDeactivated;
  private final Function<Task, Boolean> solveOnActivated;
  private final Function<Task, Boolean> solveOnDeactivated;
  private boolean isActivated = false;
  private boolean completed = false;

  public Task(
      TaskRoomGenerator trGen,
      Function<Task, Boolean> questionOnActivated,
      Function<Task, Boolean> questionOnDeactivated,
      Function<Task, Boolean> solveOnActivated,
      Function<Task, Boolean> solveOnDeactivated) {
    this.trGen = trGen;
    this.questionOnActivated = questionOnActivated;
    this.questionOnDeactivated = questionOnDeactivated;
    this.solveOnActivated = solveOnActivated;
    this.solveOnDeactivated = solveOnDeactivated;
  }

  public void question() {
    if (isActivated()) {
      questionOnActivated.apply(this);
    } else {
      setActivated(true);
      questionOnDeactivated.apply(this);
    }
  }

  public void solve() {
    if (isActivated()) {
      if (solveOnActivated.apply(this)) {
        setCompleted(true);
        if (trGen.areAllTasksCompleted()) {
          DojoStarter.openDoors(trGen.getRoom(), trGen.getNextNeighbour());
        }
      }
    } else {
      solveOnDeactivated.apply(this);
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
