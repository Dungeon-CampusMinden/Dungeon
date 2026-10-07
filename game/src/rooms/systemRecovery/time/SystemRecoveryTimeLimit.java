package rooms.systemRecovery.time;

import engine.Component;
import engine.Game;
import engine.time.PlayClock;
import java.time.Duration;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.function.Supplier;
import rooms.systemRecovery.SystemRecovery;

/**
 * Server-only one-hour budget and inactivity deadline of a run, both on the shared play clock. The
 * budget starts with the clock after the intro, survives saves through the clock's saved value and
 * stands still while the whole group pauses.
 */
public final class SystemRecoveryTimeLimit implements Component {

  /** One hour, in seconds. */
  public static final int TOTAL_SECONDS = 60 * 60;

  private static final long TOTAL_MS = TOTAL_SECONDS * 1000L;

  private final Supplier<PlayClock> clock;
  private final long hintDelayMillis;
  private long nextHintAt;
  private OptionalLong finishedAt = OptionalLong.empty();

  /** Creates the budget on the game's play clock. */
  public SystemRecoveryTimeLimit() {
    this(Duration.ofMinutes(SystemRecovery.HINT_DELAY_MINUTES), Game::playClock);
  }

  SystemRecoveryTimeLimit(Duration hintDelay, Supplier<PlayClock> clock) {
    this.clock = Objects.requireNonNull(clock, "clock");
    hintDelayMillis = hintDelay.toMillis();
    if (hintDelayMillis <= 0) throw new IllegalArgumentException("Hint delay must be positive");
  }

  /**
   * Returns the remaining budget.
   *
   * @return whole remaining seconds, rounded up and clamped at zero
   */
  public int remainingSeconds() {
    long usedMs = Math.min(clock.get().activeMs(), finishedAt.orElse(Long.MAX_VALUE));
    return (int) ((Math.max(0L, TOTAL_MS - usedMs) + 999L) / 1000L);
  }

  /**
   * Returns whether the budget ran out before the final puzzle was completed. The level editor has
   * no time limit.
   *
   * @return whether the deletion deadline has passed
   */
  public boolean expired() {
    return expiredAt(clock.get().activeMs());
  }

  /**
   * Returns whether the budget is exhausted at the given play time, for example a save's.
   *
   * @param activeMs play time to check
   * @return whether an unfinished run outside the level editor has used up its hour
   */
  public boolean expiredAt(long activeMs) {
    return finishedAt.isEmpty() && !SystemRecovery.levelEditorMode() && activeMs >= TOTAL_MS;
  }

  /**
   * Returns whether a hint may be delivered after inactivity.
   *
   * @return whether play runs and the inactivity interval has passed
   */
  public boolean hintDue() {
    return clock.get().running()
        && finishedAt.isEmpty()
        && !expired()
        && clock.get().activeMs() >= nextHintAt;
  }

  /** Restarts the inactivity interval after play starts, progress or a delivered/offered hint. */
  public void postponeHint() {
    nextHintAt = clock.get().activeMs() + hintDelayMillis;
  }

  /**
   * Restores the freeze point of a completed run from a save.
   *
   * @param finishedAtMs saved freeze point, empty while the run is unfinished
   */
  public void restoreFinish(OptionalLong finishedAtMs) {
    finishedAt = finishedAtMs;
  }

  /**
   * Returns the play time at which the countdown froze.
   *
   * @return freeze point, empty while the countdown runs
   */
  public OptionalLong finishedAt() {
    return finishedAt;
  }

  /**
   * Freezes the remaining budget once the final puzzle has been completed; a completed run never
   * expires, even when a later save restores a play time beyond the hour.
   *
   * @return active play time at which the budget froze
   */
  public long finish() {
    if (finishedAt.isEmpty()) finishedAt = OptionalLong.of(clock.get().activeMs());
    return finishedAt.getAsLong();
  }
}
