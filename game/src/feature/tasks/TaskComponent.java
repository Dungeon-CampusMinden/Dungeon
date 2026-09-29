package feature.tasks;

import engine.Component;
import engine.Entity;
import engine.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A component that manages the state and submission attempts of a {@link Task}.
 *
 * <p>The component keeps track of all submitted answers and whether the task has been solved.
 * Callbacks can be registered to react to correct and incorrect submissions.
 *
 * <p>Once a task has been solved, further submissions are rejected with a {@link TaskException}.
 *
 * @param <T> the type of answer accepted by the task
 */
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

  /**
   * Creates a new task component for the specified task.
   *
   * @param task the task managed by this component
   */
  public TaskComponent(Task<T> task) {
    this.task = task;
    this.attempts = new ArrayList<>();
  }

  /**
   * Submits an answer to the task.
   *
   * <p>The answer is recorded as an attempt and evaluated by the underlying task. If the answer is
   * correct, the task is marked as solved and the onCorrect callback is invoked. Otherwise, the
   * onWrong callback is invoked.
   *
   * @param answer the answer to submit
   * @param source the entity that submitted the answer
   * @return {@code true} if the task is solved after this submission; {@code false} otherwise
   * @throws TaskException if the task has already been solved
   */
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

  /**
   * Returns the number of attempts made so far.
   *
   * @return the number of submitted answers
   */
  public int attempts() {
    return attempts.size();
  }

  /**
   * Returns all answers that have been submitted to the task.
   *
   * @return the list of submitted answers
   */
  public List<T> getAttempts() {
    return attempts;
  }

  /**
   * Determines whether the task has been solved.
   *
   * @return {@code true} if the task is solved; {@code false} otherwise
   */
  public boolean isSolved() {
    return solved;
  }

  /**
   * Sets the solved state of the task.
   *
   * @param solved the new solved state
   */
  public void setSolved(boolean solved) {
    this.solved = solved;
  }

  /**
   * Sets the callback that is invoked when a correct answer is submitted.
   *
   * @param onCorrect the callback to invoke after a correct submission
   */
  public void onCorrect(Consumer<Entity> onCorrect) {
    this.onCorrect = onCorrect;
  }

  /**
   * Sets the callback that is invoked when an incorrect answer is submitted.
   *
   * @param onWrong the callback to invoke after an incorrect submission
   */
  public void onWrong(Consumer<Entity> onWrong) {
    this.onWrong = onWrong;
  }

  /**
   * Returns the callback invoked after a correct submission.
   *
   * @return the correct-answer callback
   */
  public Consumer<Entity> getOnCorrect() {
    return onCorrect;
  }

  /**
   * Returns the callback invoked after an incorrect submission.
   *
   * @return the incorrect-answer callback
   */
  public Consumer<Entity> getOnWrong() {
    return onWrong;
  }

  /**
   * Returns the task managed by this component.
   *
   * @return the underlying task
   */
  public Task<T> getTask() {
    return task;
  }
}
