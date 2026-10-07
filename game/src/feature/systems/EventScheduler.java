package feature.systems;

import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.System;
import feature.utils.IAction;
import java.util.PriorityQueue;

/**
 * This class is responsible for scheduling and executing timed actions. It maintains a list of
 * scheduled actions, each of which is represented by a ScheduledAction object that encapsulates a
 * IAction and an execution time. The class provides methods to schedule new actions, clear all
 * scheduled actions, and update the state of scheduled actions.
 *
 * @see IAction
 */
public class EventScheduler extends System {

  private static final PriorityQueue<ScheduledAction> scheduledActions = new PriorityQueue<>();
  private static final PriorityQueue<ScheduledAction> playActions = new PriorityQueue<>();

  /**
   * Defines whether the {@code EventScheduler} is pausable.
   *
   * <p>If set to {@code true}, no events will be triggered while the game is in a paused state. If
   * set to {@code false}, scheduled events continue to run even during pause.
   */
  private static boolean pausable = true;

  /**
   * Creates a new EventScheduler.
   *
   * <p>The EventScheduler is initialized to operate on both the client and server sides.
   *
   * @see System
   */
  public EventScheduler() {
    super(AuthoritativeSide.BOTH);
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
   * @return The scheduled action that was created and added to the list.
   */
  public static ScheduledAction scheduleAction(IAction action, long delayMillis) {
    long executeAt = TimeUtils.millis() + delayMillis;
    ScheduledAction scheduledAction = new ScheduledAction(action, executeAt);
    scheduledActions.add(scheduledAction);
    return scheduledAction;
  }

  /**
   * Schedules an authoritative gameplay action on active play time, including while reading. It
   * only fires once the room has started the play clock with {@code Game.playClock().ready()}.
   *
   * @param action action to execute
   * @param delayMillis delay in active play milliseconds
   * @return the scheduled action, cancelable via {@link #cancelAction}
   */
  public static ScheduledAction schedulePlayAction(IAction action, long delayMillis) {
    if (delayMillis < 0) throw new IllegalArgumentException("delayMillis must be non-negative");
    ScheduledAction scheduled =
        new ScheduledAction(action, Game.playClock().activeMs() + delayMillis);
    playActions.add(scheduled);
    return scheduled;
  }

  /** Drained by the engine independently of simulation-pausing dialogs. */
  public static void executePlayActions() {
    if (Game.isMultiplayerClient() || !Game.playClock().running()) return;
    long now = Game.playClock().activeMs();
    while (!playActions.isEmpty() && now >= playActions.peek().executeAt()) {
      playActions.poll().action().execute();
      if (!Game.playClock().running()) return;
    }
  }

  /**
   * Clears all scheduled actions.
   *
   * <p>This method removes all actions from the list of scheduled actions. After this method is
   * called, the list of scheduled actions will be empty.
   */
  public static void clear() {
    scheduledActions.clear();
    playActions.clear();
  }

  /**
   * Cancels specific scheduled actions from the list.
   *
   * <p>This method removes the specified scheduled action from the list of scheduled actions. If
   * the action is not found in the list, it will not be removed.
   *
   * @param scheduledAction The scheduled actions to be removed.
   * @return The removed scheduled actions.
   */
  public static ScheduledAction[] cancelAction(ScheduledAction... scheduledAction) {
    for (ScheduledAction action : scheduledAction) {
      scheduledActions.remove(action);
      playActions.remove(action);
    }
    return scheduledAction;
  }

  /**
   * Check if the given action is scheduled.
   *
   * @param action Action to check for
   * @return true if the action is scheduled, false if not.
   */
  public static boolean isScheduled(ScheduledAction action) {
    return scheduledActions.contains(action) || playActions.contains(action);
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

    while (!scheduledActions.isEmpty()) {
      ScheduledAction scheduledAction = scheduledActions.peek();
      if (currentTime < scheduledAction.executeAt) {
        break; // No more actions to execute
      }
      scheduledAction.action.execute();
      scheduledActions.poll(); // Remove the action from the queue
    }
  }

  /**
   * Represents a scheduled action with an associated action and execution time. Implements
   * Comparable to allow sorting within the PriorityQueue based on execution time.
   *
   * <p>This action is scheduled to be executed at a specific time in the future via the {@link
   * EventScheduler}.
   *
   * @param action The action to be executed.
   * @param executeAt The time at which the action should be executed, in milliseconds.
   */
  public record ScheduledAction(IAction action, long executeAt)
      implements Comparable<ScheduledAction> {
    @Override
    public int compareTo(ScheduledAction other) {
      // Sort by execution time (earliest first)
      return Long.compare(this.executeAt, other.executeAt);
    }
  }

  /**
   * Sets whether the {@code EventScheduler} is pausable.
   *
   * @param value {@code true} if no events should be triggered while the game is paused, {@code
   *     false} if events should continue running even during pause
   */
  public static void setPausable(boolean value) {
    pausable = value;
  }

  @Override
  public void stop() {
    if (pausable) run = false;
    else run = true;
  }
}
