package feature.interaction.cardreader;

import static feature.interaction.cardreader.Pace.encode;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PaceTest {

  private static final Pace PACE = CardReaderFactory.DEFAULT_PACE;

  @Test
  void swipeAtAModeratePaceIsAccepted() {
    assertEquals(Pace.Result.ACCEPTED, PACE.rate(encode(600, true)));
    assertEquals(Pace.Result.ACCEPTED, PACE.rate(encode(300, true)));
    assertEquals(Pace.Result.ACCEPTED, PACE.rate(encode(1200, true)));
  }

  @Test
  void quickFlickIsTooFast() {
    assertEquals(Pace.Result.TOO_FAST, PACE.rate(encode(299, true)));
    assertEquals(Pace.Result.TOO_FAST, PACE.rate(encode(80, true)));
  }

  @Test
  void dawdlingIsTooSlow() {
    assertEquals(Pace.Result.TOO_SLOW, PACE.rate(encode(1201, true)));
    assertEquals(Pace.Result.TOO_SLOW, PACE.rate(encode(5000, true)));
  }

  @Test
  void cardLetGoBeforeTheEndIsNotRecognizedWhateverThePace() {
    assertEquals(Pace.Result.INCOMPLETE, PACE.rate(encode(600, false)));
    assertEquals(Pace.Result.INCOMPLETE, PACE.rate(encode(80, false)));
  }

  @Test
  void malformedAnswersOpenNothing() {
    assertEquals(Pace.Result.INCOMPLETE, PACE.rate(""));
    assertEquals(Pace.Result.INCOMPLETE, PACE.rate("600"));
    assertEquals(Pace.Result.INCOMPLETE, PACE.rate("fast:true"));
    assertEquals(Pace.Result.INCOMPLETE, PACE.rate("600:true:true"));
  }
}
