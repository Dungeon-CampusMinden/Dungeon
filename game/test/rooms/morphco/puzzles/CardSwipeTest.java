package rooms.morphco.puzzles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CardSwipeTest {

  @Test
  void swipeAtAModeratePaceIsAccepted() {
    assertEquals(CardSwipe.Result.ACCEPTED, CardSwipe.rate(CardSwipe.encode(600, true)));
    assertEquals(CardSwipe.Result.ACCEPTED, CardSwipe.rate(CardSwipe.encode(300, true)));
    assertEquals(CardSwipe.Result.ACCEPTED, CardSwipe.rate(CardSwipe.encode(1200, true)));
  }

  @Test
  void quickFlickIsTooFast() {
    assertEquals(CardSwipe.Result.TOO_FAST, CardSwipe.rate(CardSwipe.encode(299, true)));
    assertEquals(CardSwipe.Result.TOO_FAST, CardSwipe.rate(CardSwipe.encode(80, true)));
  }

  @Test
  void dawdlingIsTooSlow() {
    assertEquals(CardSwipe.Result.TOO_SLOW, CardSwipe.rate(CardSwipe.encode(1201, true)));
    assertEquals(CardSwipe.Result.TOO_SLOW, CardSwipe.rate(CardSwipe.encode(5000, true)));
  }

  @Test
  void cardLetGoBeforeTheEndIsNotRecognizedWhateverThePace() {
    assertEquals(CardSwipe.Result.INCOMPLETE, CardSwipe.rate(CardSwipe.encode(600, false)));
    assertEquals(CardSwipe.Result.INCOMPLETE, CardSwipe.rate(CardSwipe.encode(80, false)));
  }

  @Test
  void malformedAnswersOpenNothing() {
    assertEquals(CardSwipe.Result.INCOMPLETE, CardSwipe.rate(""));
    assertEquals(CardSwipe.Result.INCOMPLETE, CardSwipe.rate("600"));
    assertEquals(CardSwipe.Result.INCOMPLETE, CardSwipe.rate("fast:true"));
    assertEquals(CardSwipe.Result.INCOMPLETE, CardSwipe.rate("600:true:true"));
  }
}
