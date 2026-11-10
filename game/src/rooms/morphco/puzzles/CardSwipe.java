package rooms.morphco.puzzles;

/**
 * The card reader at the entrance: the security card has to be swiped through the slot at the right
 * pace.
 *
 * <p>The client measures how long the swipe took and sends it encoded with {@link #encode(long,
 * boolean)}. The server rates it again with {@link #rate(String)} and opens the door only for
 * {@link Result#ACCEPTED}.
 */
public final class CardSwipe {

  /** Outcome of one swipe. */
  public enum Result {
    /** Right pace, the door opens. */
    ACCEPTED,
    /** Through the slot in less than 0.3 s. */
    TOO_FAST,
    /** Through the slot in more than 1.2 s. */
    TOO_SLOW,
    /** Let go before the card reached the end of the slot. */
    INCOMPLETE
  }

  /** Fastest accepted swipe. Among Us asks for about 0.5 s; the reader is more forgiving. */
  private static final long MIN_MILLIS = 300;

  /** Slowest accepted swipe. */
  private static final long MAX_MILLIS = 1200;

  private CardSwipe() {}

  /**
   * Encodes a swipe for the dialog callback.
   *
   * @param millis how long the swipe took
   * @param complete whether the card was at the end of the slot when it was let go
   * @return the answer for the server
   */
  public static String encode(long millis, boolean complete) {
    return millis + ":" + complete;
  }

  /**
   * Rates an encoded swipe.
   *
   * @param swipe answer from {@link #encode(long, boolean)}
   * @return the outcome; malformed answers count as not recognized
   */
  public static Result rate(String swipe) {
    String[] parts = swipe.split(":", -1);
    if (parts.length != 2 || !Boolean.parseBoolean(parts[1])) return Result.INCOMPLETE;
    long millis;
    try {
      millis = Long.parseLong(parts[0]);
    } catch (NumberFormatException e) {
      return Result.INCOMPLETE;
    }
    if (millis < MIN_MILLIS) return Result.TOO_FAST;
    if (millis > MAX_MILLIS) return Result.TOO_SLOW;
    return Result.ACCEPTED;
  }
}
