package systems;

import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.badlogic.gdx.utils.TimeUtils;
import core.System;
import utils.TimedEffect;

/**
 * EventScheduler is a class that extends the System class.
 *
 * <p>This class is responsible for scheduling and executing timed actions. It maintains a list of
 * scheduled actions, each of which is represented by a ScheduledAction object that encapsulates a
 * TimedEffect and an execution time. The class provides methods to schedule new actions, clear all
 * scheduled actions, and update the state of scheduled actions.
 *
 * <p>The EventScheduler class follows the Singleton design pattern, meaning that only one instance
 * of the class can exist. The getInstance() method is used to get the instance of the class.
 */
public class EventScheduler extends System {

  private static EventScheduler INSTANCE;
  private final DelayedRemovalArray<ScheduledAction> scheduledActions = new DelayedRemovalArray<>();

  private EventScheduler() {}

  public static EventScheduler getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new EventScheduler();
    }
    return INSTANCE;
  }

  /**
   * Updates the state of scheduled actions.
   *
   * <p>This method iterates over the list of scheduled actions and checks if the current time is
   * greater than or equal to the execution time of each action. If it is, the action's effect is
   * applied and the action is removed from the list. The method uses the begin() and end() methods
   * of the DelayedRemovalArray class to safely modify the array during iteration.
   *
   * <p>This method should be called once per frame to ensure that scheduled actions are executed
   */
  public void update() {
    long currentTime = TimeUtils.millis();
    this.scheduledActions.begin();
    for (int i = 0; i < this.scheduledActions.size; i++) {
      ScheduledAction scheduledAction = this.scheduledActions.get(i);
      if (currentTime >= scheduledAction.executeAt) {
        scheduledAction.action.applyEffect();
        this.scheduledActions.removeIndex(i);
      }
    }
    this.scheduledActions.end();
  }

  /**
   * Schedules a new action to be executed after a specified delay.
   *
   * <p>This method creates a new ScheduledAction with the provided action and execution time, and
   * adds it to the list of scheduled actions. The execution time is calculated as the current time
   * plus the provided delay.
   *
   * @param action The action to be executed.
   * @param delayMillis The delay in milliseconds after which the action should be executed.
   */
  public void scheduleAction(TimedEffect action, long delayMillis) {
    long executeAt = TimeUtils.millis() + delayMillis;
    this.scheduledActions.add(new ScheduledAction(action, executeAt));
  }

  /**
   * Clears all scheduled actions.
   *
   * <p>This method removes all actions from the list of scheduled actions. After this method is
   * called, the list of scheduled actions will be empty.
   */
  public void clear() {
    this.scheduledActions.clear();
  }

  /**
   * Executes the update method.
   *
   * <p>This method is an override of the execute method in the parent class. When called, it
   * executes the update method of this class.
   */
  @Override
  public void execute() {
    this.update();
  }

  private static class ScheduledAction {
    TimedEffect action;
    long executeAt;

    ScheduledAction(TimedEffect action, long executeAt) {
      this.action = action;
      this.executeAt = executeAt;
    }
  }
}
