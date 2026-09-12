package feature.tasks;

import engine.Component;
import engine.Entity;
import java.util.function.Consumer;

public class TaskComponent<T> implements Component {
  private Task<T> task;
  private boolean solved;
  private int attempts;
  private Consumer<Entity> onCorrect =
      (e) -> {
        System.out.println("CORRECT");
      };
  private Consumer<Entity> onWrong =
      (e) -> {
        System.out.println("WRONG");
      };

  public TaskComponent(Task<T> task) {
    this.task = task;
  }

  public boolean submit(T answer, Entity source) {
    if (solved) {
      return true;
    }

    if (task.isCorrect(answer)) {
      onCorrect.accept(source);
      solved = true;
    } else {
      attempts++;
      onWrong.accept(source);
    }

    return solved;
  }

  public int attempts() {
    return attempts;
  }

  public boolean isSolved() {
    return solved;
  }

  public void setSolved(boolean solved) {
    this.solved = solved;
  }

  public void onCorrect(Consumer<Entity> onCorrect) {
    this.onCorrect = onCorrect;
  }

  public void onWrong(Consumer<Entity> onWrong) {
    this.onWrong = onWrong;
  }

  public Consumer<Entity> getOnCorrect() {
    return onCorrect;
  }

  public Consumer<Entity> getOnWrong() {
    return onWrong;
  }

  public Task<T> getTask() {
    return task;
  }

  public void setAttempts(int attempts) {
    this.attempts = attempts;
  }
}
