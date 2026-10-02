package rooms.lasthour.modules.computer;

/** Converts the blog's wall-clock login timestamp to a pauseable save value. */
public final class LastHourBlogTime {
  private LastHourBlogTime() {}

  public static int elapsedSeconds(int loginTimestamp, int nowSeconds) {
    return loginTimestamp <= 0 ? 0 : Math.max(0, nowSeconds - loginTimestamp);
  }

  public static int rebasedLoginTimestamp(int savedElapsedSeconds, int nowSeconds) {
    if (savedElapsedSeconds < 0) throw new IllegalArgumentException("Negative blog elapsed time");
    return nowSeconds - savedElapsedSeconds;
  }
}
