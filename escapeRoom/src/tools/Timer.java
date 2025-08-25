package tools;

import contrib.hud.dialogs.OkDialog;
import contrib.systems.EventScheduler;
import core.Game;

/**
 * Utility class for scheduling timed events in the game.
 *
 * <p>Currently provides a method to start a countdown timer that triggers the end of the game.
 */
public class Timer {

  /**
   * Schedules an action to show a "TIME OVER" dialog and end the game after the specified delay.
   *
   * @param milliseconds the delay in milliseconds before the action is executed
   * @return a ScheduledAction representing the scheduled event
   */
  public static EventScheduler.ScheduledAction startTimeToEndGame(int milliseconds) {
    return EventScheduler.scheduleAction(
        () -> OkDialog.showOkDialog("TIME OVER", "GAME OVER", () -> Game.exit()), milliseconds);
  }
}
