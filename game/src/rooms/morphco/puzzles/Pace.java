package rooms.morphco.puzzles;

/**
 * A movement that only counts at the right pace, like the card through the reader or the lever of
 * A-12.
 *
 * <p>The client measures how long the movement took to its end and sends it encoded with {@link
 * #encode(long, boolean)}. The server rates it again with {@link #rate(String)}.
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
   * Encodes a movement for the dialog callback.
   *
   * @param millis how long the movement took to its end
   * @param complete whether the end was reached when it was let go
   * @return the answer for the server
   */
  public static String encode(long millis, boolean complete) {
    return millis + ":" + complete;
  }

  /**
   * Rates an encoded movement.
   *
   * @param movement answer from {@link #encode(long, boolean)}
   * @return the outcome; malformed answers count as incomplete
   */
  public Result rate(String movement) {
    String[] parts = movement.split(":", -1);
    if (parts.length != 2 || !Boolean.parseBoolean(parts[1])) return Result.INCOMPLETE;
    long millis;
    try {
      millis = Long.parseLong(parts[0]);
    } catch (NumberFormatException e) {
      return Result.INCOMPLETE;
    }
    if (millis < minMillis) return Result.TOO_FAST;
    if (millis > maxMillis) return Result.TOO_SLOW;
    return Result.ACCEPTED;
  }
}
