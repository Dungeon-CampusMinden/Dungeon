package rooms.lasthour.modules.computer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LastHourBlogTimeTest {
  @Test
  void offlineTimeDoesNotUnlockMoreBlogComments() {
    int login = 1_000;
    int saveAt = 1_090;
    int loadAt = 10_000;

    int elapsed = LastHourBlogTime.elapsedSeconds(login, saveAt);
    int restoredLogin = LastHourBlogTime.rebasedLoginTimestamp(elapsed, loadAt);

    assertEquals(90, LastHourBlogTime.elapsedSeconds(restoredLogin, loadAt));
    assertEquals(100, LastHourBlogTime.elapsedSeconds(restoredLogin, loadAt + 10));
  }
}
