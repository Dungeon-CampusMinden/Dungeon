package rooms.lasthour.modules.computer;

/** Converts the blog's encoded login play time to elapsed active seconds. */
public final class LastHourBlogTime {
  private LastHourBlogTime() {}

  /**
   * Calculates how long the blog session has been active.
   *
   * @param loginTimestamp login play seconds plus one; zero means not logged in
   * @param nowSeconds current active play time in seconds
   * @return elapsed session time in seconds
   */
  public static int elapsedSeconds(int loginTimestamp, int nowSeconds) {
    return loginTimestamp <= 0 ? 0 : Math.max(0, nowSeconds - (loginTimestamp - 1));
  }

  /**
   * Recreates a login timestamp from saved elapsed time.
   *
   * @param savedElapsedSeconds elapsed session time saved in seconds
   * @param nowSeconds current active play time in seconds
   * @return reconstructed login play seconds plus one; zero means not logged in
   */
  public static int rebasedLoginTimestamp(int savedElapsedSeconds, int nowSeconds) {
    if (savedElapsedSeconds < 0) throw new IllegalArgumentException("Negative blog elapsed time");
    return nowSeconds - savedElapsedSeconds + 1;
  }
}
