package feature.timer;

import engine.Component;
import engine.Game;

/**
 * Countdown measured against the server's active play clock. It stands still until the room starts
 * the clock with {@code Game.playClock().ready()}. A stopwatch counts the elapsed time up instead.
 *
 * @param startedAtActiveMs active play time when the countdown began
 * @param duration the duration of the timer in seconds
 * @param stoppedAtActiveMs active play time at which the countdown froze, {@link Long#MAX_VALUE}
 *     while it runs
 * @param countsUp whether the timer shows the elapsed instead of the remaining time
 */
public record WorldTimerComponent(
    long startedAtActiveMs, int duration, long stoppedAtActiveMs, boolean countsUp)
    implements Component {

  /**
   * Creates a countdown.
   *
   * @param startedAtActiveMs active play time when the countdown began
   * @param duration the duration of the timer in seconds
   * @param stoppedAtActiveMs active play time at which the countdown froze, {@link Long#MAX_VALUE}
   *     while it runs
   */
  public WorldTimerComponent(long startedAtActiveMs, int duration, long stoppedAtActiveMs) {
    this(startedAtActiveMs, duration, stoppedAtActiveMs, false);
  }

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
   * Creates a running stopwatch.
   *
   * @param startedAtActiveMs active play time from which the stopwatch counts
   * @return stopwatch
   */
  public static WorldTimerComponent stopwatch(long startedAtActiveMs) {
    return new WorldTimerComponent(startedAtActiveMs, 0, Long.MAX_VALUE, true);
  }

  /**
   * Returns the milliseconds since the timer started on the current play clock.
   *
   * @return elapsed milliseconds, never negative
   */
  public long elapsedMs() {
    long activeMs = Math.min(Game.playClock().activeMs(), stoppedAtActiveMs);
    return Math.max(0, activeMs - startedAtActiveMs);
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
   * Returns this timer frozen at the given play time.
   *
   * @param activeMs active play time at which the timer stops
   * @return frozen timer
   */
  public WorldTimerComponent stoppedAt(long activeMs) {
    return new WorldTimerComponent(startedAtActiveMs, duration, activeMs, countsUp);
  }
}
