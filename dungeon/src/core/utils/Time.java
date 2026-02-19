package core.utils;

/** Utility class for time-related operations. */
public final class Time {
  private Time() {}

  public static long nowMs() {
    return java.lang.System.currentTimeMillis();
  }

  public static long nowNs() {
    return java.lang.System.nanoTime();
  }

  public static long sinceMs(long timestampMs) {
    return nowMs() - timestampMs;
  }
}
