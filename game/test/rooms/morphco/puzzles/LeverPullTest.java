package rooms.morphco.puzzles;

import static feature.interaction.cardreader.Pace.encode;
import static org.junit.jupiter.api.Assertions.assertEquals;

import feature.interaction.cardreader.Pace;
import org.junit.jupiter.api.Test;

class LeverPullTest {

  private static final Pace PACE = LeverPull.PACE;

  @Test
  void evenPullRestartsTheSorter() {
    assertEquals(Pace.Result.ACCEPTED, PACE.rate(encode(600, true)));
    assertEquals(Pace.Result.ACCEPTED, PACE.rate(encode(1200, true)));
    assertEquals(Pace.Result.ACCEPTED, PACE.rate(encode(2000, true)));
  }

  @Test
  void jerkAndDawdleAreRejected() {
    assertEquals(Pace.Result.TOO_FAST, PACE.rate(encode(599, true)));
    assertEquals(Pace.Result.TOO_FAST, PACE.rate(encode(250, true)));
    assertEquals(Pace.Result.TOO_SLOW, PACE.rate(encode(2001, true)));
  }
}
