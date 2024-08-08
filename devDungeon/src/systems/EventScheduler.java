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

  /**
   * Gets the instance of the EventScheduler class.
   *
   * <p>Singleton design pattern is used to ensure that only one instance of the class can exist.
   * The getInstance() method is used to get the instance of the class.
   *
   * @return The instance of the EventScheduler class.
   */
  public static EventScheduler getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new EventScheduler();
    }
    return INSTANCE;
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
    scheduledActions.add(new ScheduledAction(action, executeAt));
  }

  /**
   * Clears all scheduled actions.
   *
   * <p>This method removes all actions from the list of scheduled actions. After this method is
   * called, the list of scheduled actions will be empty.
   */
  public void clear() {
    scheduledActions.clear();
  }

  /**
   * Executes the scheduled actions.
   *
   * <p>This method is called to process all the scheduled actions. It first gets the current time,
   * then it starts iterating over the scheduled actions. For each scheduled action, it checks if
   * the current time is greater than or equal to the execution time of the action. If it is, it
   * applies the effect of the action and removes the action from the list of scheduled actions.
   * After all actions have been processed, it ends the iteration over the scheduled actions.
   */
  @Override
  public void execute() {
    long currentTime = TimeUtils.millis();
    scheduledActions.begin();
    for (int i = 0; i < scheduledActions.size; i++) {
      ScheduledAction scheduledAction = scheduledActions.get(i);
      if (currentTime >= scheduledAction.executeAt) {
        scheduledAction.action.applyEffect();
        scheduledActions.removeIndex(i);
      }
    }
    scheduledActions.end();
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
