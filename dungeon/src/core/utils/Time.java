package core.utils;

/**
 * A utility class for time-related operations.
 *
 * <p>This class provides static methods to retrieve the current time in different precisions
 * and to calculate elapsed time since a given timestamp.
 */
public final class Time {
  private Time() {}

  /**
   * Gets the current system time in milliseconds.
   *
   * @return current time in milliseconds since epoch
   */
  public static long nowMs() {
    return System.currentTimeMillis();
  }

  /**
   * Gets the current system time in nanoseconds.
   *
   * @return current time in nanoseconds since an arbitrary fixed origin point
   */
  public static long nowNs() {
    return System.nanoTime();
  }

  /**
   * Calculates the elapsed time in milliseconds since a given timestamp.
   *
   * @param timestampMs a timestamp in milliseconds from a previous call to {@link #nowMs()}
   * @return elapsed time in milliseconds
   */
  public static long sinceMs(long timestampMs) {
    return nowMs() - timestampMs;
  }
}
