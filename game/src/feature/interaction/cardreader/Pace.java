package feature.interaction.cardreader;

/**
 * A movement that only counts at the right pace, like the card through a card reader.
 *
 * <p>The client measures how long the movement took and rates it with {@link #rate(long, boolean)}
 * right away; the server rates it again.
 *
 * @param minMillis fastest accepted movement
 * @param maxMillis slowest accepted movement
 */
public record Pace(long minMillis, long maxMillis) {

  /** Outcome of one movement. */
  public enum Result {
    /** Right pace. */
    ACCEPTED,
    /** Faster than the minimum. */
    TOO_FAST,
    /** Slower than the maximum. */
    TOO_SLOW,
    /** Let go before the end. */
    INCOMPLETE
  }

  /**
   * Rates a movement.
   *
   * @param millis how long the movement took
   * @param complete whether the end was reached when it was let go
   * @return the outcome
   */
  public Result rate(long millis, boolean complete) {
    if (!complete) return Result.INCOMPLETE;
    if (millis < minMillis) return Result.TOO_FAST;
    if (millis > maxMillis) return Result.TOO_SLOW;
    return Result.ACCEPTED;
  }
}
