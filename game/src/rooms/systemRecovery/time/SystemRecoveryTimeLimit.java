package rooms.systemRecovery.time;

import engine.Component;
import java.time.Duration;
import java.util.Objects;
import java.util.function.LongSupplier;
import rooms.systemRecovery.SystemRecovery;

/** Server-only runtime component for one run's countdown and inactivity deadline. */
public final class SystemRecoveryTimeLimit implements Component {

  /** One hour, in seconds. */
  public static final int TOTAL_SECONDS = 60 * 60;

  private final LongSupplier clock;
  private final long hintDelayMillis;
  private long expiresAt;
  private long nextHintAt;
  private boolean started;
  private boolean finished;
  private int frozenSeconds = TOTAL_SECONDS;

  /** Creates a clock that is independent of system-clock corrections. */
  public SystemRecoveryTimeLimit() {
    this(
        Duration.ofMinutes(SystemRecovery.HINT_DELAY_MINUTES),
        () -> System.nanoTime() / 1_000_000L);
  }

  SystemRecoveryTimeLimit(Duration hintDelay, LongSupplier clock) {
    this.clock = Objects.requireNonNull(clock, "clock");
    hintDelayMillis = hintDelay.toMillis();
    if (hintDelayMillis <= 0) throw new IllegalArgumentException("Hint delay must be positive");
  }

  /** Starts once after the opening cutscene; another player's intro cannot reset the budget. */
  public void start() {
    if (!started) restore(TOTAL_SECONDS);
  }

  /**
   * Resumes a stored budget without charging time spent outside the running game.
   *
   * @param remainingSeconds validated save budget
   */
  public void restore(int remainingSeconds) {
    if (remainingSeconds < 0 || remainingSeconds > TOTAL_SECONDS) {
      throw new IllegalArgumentException("Remaining time must be between zero and one hour");
    }
    started = true;
    finished = false;
    expiresAt = clock.getAsLong() + remainingSeconds * 1000L;
    postponeHint();
  }

  /**
   * @return whole remaining seconds, rounded up and clamped at zero
   */
  public int remainingSeconds() {
    if (!started || finished) return frozenSeconds;
    return (int) ((Math.max(0L, expiresAt - clock.getAsLong()) + 999L) / 1000L);
  }

  /**
   * @return whether the opening cutscene has started the countdown
   */
  public boolean started() {
    return started;
  }

  /**
   * @return whether the active countdown has exhausted its budget
   */
  public boolean expired() {
    return started && !finished && remainingSeconds() == 0;
  }

  /**
   * @return whether a hint may be delivered after inactivity
   */
  public boolean hintDue() {
    return started && !finished && !expired() && clock.getAsLong() >= nextHintAt;
  }

  /** Restarts the inactivity interval after progress or a delivered/offered hint. */
  public void postponeHint() {
    nextHintAt = clock.getAsLong() + hintDelayMillis;
  }

  /** Freezes the remaining budget after the final puzzle has been completed. */
  public void finish() {
    frozenSeconds = remainingSeconds();
    finished = true;
  }
}
