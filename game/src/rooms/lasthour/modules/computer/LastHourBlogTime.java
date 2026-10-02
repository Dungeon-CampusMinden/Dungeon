package rooms.lasthour.modules.computer;

/** Converts the blog's wall-clock login timestamp to a pauseable save value. */
public final class LastHourBlogTime {
  private LastHourBlogTime() {}

  /**
   * Calculates how long the blog session has been active.
   *
   * @param loginTimestamp login time in epoch seconds
   * @param nowSeconds current time in epoch seconds
   * @return elapsed session time in seconds
   */
  public static int elapsedSeconds(int loginTimestamp, int nowSeconds) {
    return loginTimestamp <= 0 ? 0 : Math.max(0, nowSeconds - loginTimestamp);
  }

  /**
   * Recreates a login timestamp from saved elapsed time.
   *
   * @param savedElapsedSeconds elapsed session time saved in seconds
   * @param nowSeconds current time in epoch seconds
   * @return reconstructed login time in epoch seconds
   */
  public static int rebasedLoginTimestamp(int savedElapsedSeconds, int nowSeconds) {
    if (savedElapsedSeconds < 0) throw new IllegalArgumentException("Negative blog elapsed time");
    return nowSeconds - savedElapsedSeconds;
  }
}
