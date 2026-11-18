package rooms.morphco.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import feature.interaction.cardreader.Pace;
import org.junit.jupiter.api.Test;

class LeverPullTest {

  private static final Pace PACE = LeverPull.PACE;

  @Test
  void evenPullRestartsTheSorter() {
    assertEquals(Pace.Result.ACCEPTED, PACE.rate(600, true));
    assertEquals(Pace.Result.ACCEPTED, PACE.rate(1200, true));
    assertEquals(Pace.Result.ACCEPTED, PACE.rate(2000, true));
  }

  @Test
  void jerkAndDawdleAreRejected() {
    assertEquals(Pace.Result.TOO_FAST, PACE.rate(599, true));
    assertEquals(Pace.Result.TOO_FAST, PACE.rate(250, true));
    assertEquals(Pace.Result.TOO_SLOW, PACE.rate(2001, true));
  }
}
