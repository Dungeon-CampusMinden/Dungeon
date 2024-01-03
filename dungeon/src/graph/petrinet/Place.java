package graph.petrinet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import task.Task;

/**
 * Represents a Place in the Petri Net.
 *
 * <p>Stores an integer value as tokens. Add a token to the Place by calling {@link #placeToken()},
 * use {@link #removeToken()} to decrease the token count.
 *
 * <p>Places can be observed by {@link Transition}. If a new token is added to a Place, it will
 * notify all observer transitions. Use {@link #register(Transition)} to register a Transition as an
 * observer.
 *
 * <p>A Place can change the {@link task.Task.TaskState} of a {@link Task} if a token is added. Add
 * a {@link Task} whose state should be changed via {@link #changeStateOnTokenAdd(Task,
 * task.Task.TaskState)}.
 *
 * <p>Another option is that a {@link Place} observes a {@link Task}, and if the Task changes its
 * state, a token is added to this place. Register a Task to observe via {@link #observe(Task,
 * Task.TaskState)}.
 *
 * <p>A Place can only observe a Task OR change the Task state. A combination of both is not
 * possible. Transitions are not impacted by this limitation.
 */
public class Place {
  private int tokenCount = 0;
  private final Map<Task, Task.TaskState> observe = new HashMap<>();
  private final Map<Task, Task.TaskState> changeStateOnTokenAdd = new HashMap<>();
  private final Set<Transition> transition = new HashSet<>();

  /**
   * Add a Task to observe. If the Task changes its state to the given state, this place will
   * increase its token count.
   *
   * @param task Task to observe.
   * @param state Add a token to this place if the given task reaches this state.
   */
  public void observe(Task task, Task.TaskState state) {
    if (!changeStateOnTokenAdd.isEmpty())
      throw new RuntimeException("A Place cannot observe and activate Tasks at the same time.");
    task.registerPlace(this);
    observe.put(task, state);
  }

  /**
   * Notify this Place that a Task has changed its state.
   *
   * @param task Task that changed its state.
   * @param state State to which it has changed.
   */
  public void notify(Task task, Task.TaskState state) {
    if (observe.get(task) == state) placeToken();
  }

  /**
   * Add a Task whose state should be set to the given state if this place increases its token
   * count.
   *
   * @param task Task whose state should be changed.
   * @param state State to be set.
   */
  public void changeStateOnTokenAdd(Task task, Task.TaskState state) {
    if (!observe.isEmpty())
      throw new RuntimeException("A Place cannot observe and activate Tasks at the same time.");
    changeStateOnTokenAdd.put(task, state);
  }

  /**
   * Increase the token count of this place by one.
   *
   * <p>Will inform each observing {@link Transition} and will set the states of Tasks if added via
   * {@link #changeStateOnTokenAdd(Task, Task.TaskState)}.
   */
  public void placeToken() {
    tokenCount++;
    changeStateOnTokenAdd.forEach(Task::state);
    transition.forEach(transition -> transition.notify(this));
  }

  /**
   * Decrease the token count of this place.
   *
   * <p>Token-count cant be negative.
   *
   * <p>This will invoke {@link Transition#notify(Place)} for all observers.
   */
  public void removeToken() {
    tokenCount = Math.max(0, tokenCount - 1);
    transition.forEach(transition -> transition.notify(this));
  }

  /**
   * Register a {@link Transition} as an observer.
   *
   * <p>Observers are notified by {@link Transition#notify(Place)} when a token is added to this
   * place.
   *
   * @param observer The Transition serving as the observer.
   */
  public void register(Transition observer) {
    this.transition.add(observer);
  }

  /**
   * Retrieve the number of tokens in this place.
   *
   * @return The number of tokens in this place.
   */
  public int tokenCount() {
    return tokenCount;
  }
}
