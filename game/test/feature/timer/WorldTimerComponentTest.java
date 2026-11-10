package feature.timer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link WorldTimerComponent}. */
class WorldTimerComponentTest {

  @AfterEach
  void resetClock() {
    Game.playClock().synchronize(0, false);
  }

  @Test
  void stopwatchCountsThePlayTimeSinceItsStart() {
    Game.playClock().synchronize(90_500, false);

    WorldTimerComponent stopwatch = WorldTimerComponent.stopwatch(30_000);

    assertEquals(60_500, stopwatch.elapsedMs());
    // A stopwatch has no countdown left, so it never triggers the last-minute alarm.
    assertEquals(0, stopwatch.remainingMs());
  }

  @Test
  void stoppedStopwatchKeepsCountingUpAndFreezes() {
    Game.playClock().synchronize(90_000, false);

    WorldTimerComponent stopped = WorldTimerComponent.stopwatch(0).stoppedAt(40_000);

    assertTrue(stopped.countsUp());
    assertEquals(40_000, stopped.elapsedMs());
  }

  @Test
  void countdownShowsTheRemainingTime() {
    Game.playClock().synchronize(10_000, false);

    WorldTimerComponent countdown = new WorldTimerComponent(0, 60);

    assertEquals(50_000, countdown.remainingMs());
    assertEquals(10_000, countdown.elapsedMs());
  }
}
