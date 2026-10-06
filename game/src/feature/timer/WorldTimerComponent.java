package feature.timer;

import engine.Component;
import engine.Game;

/**
 * Countdown measured against the server's active play clock. It stands still until the room starts
 * the clock with {@code Game.playClock().ready()}.
 *
 * @param startedAtActiveMs active play time when the countdown began
 * @param duration the duration of the timer in seconds
 */
public record WorldTimerComponent(long startedAtActiveMs, int duration) implements Component {
  /** Returns the countdown's remaining milliseconds on the current play clock. */
  public long remainingMs() {
    return Math.max(0, duration * 1000L - (Game.playClock().activeMs() - startedAtActiveMs));
  }
}
