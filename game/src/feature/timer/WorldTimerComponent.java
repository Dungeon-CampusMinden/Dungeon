package feature.timer;

import engine.Component;
import engine.Game;

/**
 * Countdown measured against the server's active play clock. It stands still until the room starts
 * the clock with {@code Game.playClock().ready()}.
 *
 * @param startedAtActiveMs active play time when the countdown began
 * @param duration the duration of the timer in seconds
 * @param stoppedAtActiveMs active play time at which the countdown froze, {@link Long#MAX_VALUE}
 *     while it runs
 */
public record WorldTimerComponent(long startedAtActiveMs, int duration, long stoppedAtActiveMs)
    implements Component {

  /**
   * Creates a running countdown.
   *
   * @param startedAtActiveMs active play time when the countdown began
   * @param duration the duration of the timer in seconds
   */
  public WorldTimerComponent(long startedAtActiveMs, int duration) {
    this(startedAtActiveMs, duration, Long.MAX_VALUE);
  }

  /**
   * Returns the countdown's remaining milliseconds on the current play clock.
   *
   * @return remaining milliseconds, never negative
   */
  public long remainingMs() {
    long activeMs = Math.min(Game.playClock().activeMs(), stoppedAtActiveMs);
    return Math.max(0, duration * 1000L - (activeMs - startedAtActiveMs));
  }

  /**
   * Returns this countdown frozen at the given play time.
   *
   * @param activeMs active play time at which the countdown stops
   * @return frozen countdown
   */
  public WorldTimerComponent stoppedAt(long activeMs) {
    return new WorldTimerComponent(startedAtActiveMs, duration, activeMs);
  }
}
