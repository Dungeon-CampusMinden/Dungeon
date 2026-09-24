package feature.tasks;

import engine.Component;
import engine.Entity;
import engine.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TaskComponent<T> implements Component {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TaskComponent.class);

  private final Task<T> task;
  private boolean solved;
  private List<T> attempts;
  private Consumer<Entity> onCorrect =
      (e) -> {
        LOGGER.info("Task was solved in {} attempts", attempts());
      };
  private Consumer<Entity> onWrong =
      (e) -> {
        LOGGER.info("Task failed, now at {} attempts", attempts());
      };

  public TaskComponent(Task task) {
    this.task = task;
    this.attempts = new ArrayList<>();
  }


  public boolean submit(T answer, Entity source) throws TaskException {
    if (this.solved) {
      throw new TaskException("Task already solved.");
    }

    this.attempts.add(answer);

    if (task.isCorrect(answer)) {
      onCorrect.accept(source);
      this.solved = true;
    } else {
      onWrong.accept(source);
    }

    return this.solved;
  }

  public int attempts() {
    return attempts.size();
  }

  public List<T> getAttempts() {
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

  public Task getTask() {
    return task;
  }
}
