package feature.audience;

import static org.junit.jupiter.api.Assertions.assertEquals;

import feature.hud.dialogs.ChoiceOption;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests when voting windows end early. */
class AudienceSystemTest {

  private static final long NOW = 10_000;
  private static final long END = NOW + 120_000;

  private static final AudienceContext CHOICE =
      new AudienceContext.Choice(null, List.of(ChoiceOption.of("A", "a")), false);

  @Test
  void puzzleKeepsItsTimeUntilEverybodyVoted() {
    assertEquals(END, AudienceSystem.windowEnd(CHOICE, END, 0, 3, NOW));
    assertEquals(END, AudienceSystem.windowEnd(CHOICE, END, 2, 3, NOW));
  }

  @Test
  void puzzleClosesSoonOnceEverybodyVoted() {
    assertEquals(NOW + 3_000, AudienceSystem.windowEnd(CHOICE, END, 3, 3, NOW));
    assertEquals(
        NOW + 3_000, AudienceSystem.windowEnd(new AudienceContext.Text(null, 4), END, 3, 3, NOW));
  }

  @Test
  void earlierEndStays() {
    assertEquals(NOW + 1_000, AudienceSystem.windowEnd(CHOICE, NOW + 1_000, 3, 3, NOW));
  }

  @Test
  void textClosesSoonOnceHalfFinishedReading() {
    AudienceContext text = new AudienceContext.Continue(null);
    assertEquals(END, AudienceSystem.windowEnd(text, END, 1, 3, NOW));
    assertEquals(NOW + 5_000, AudienceSystem.windowEnd(text, END, 2, 4, NOW));
  }

  @Test
  void textClosesFasterOnceEverybodyFinishedReading() {
    AudienceContext text = new AudienceContext.Continue(null);
    assertEquals(NOW + 2_000, AudienceSystem.windowEnd(text, END, 4, 4, NOW));
    assertEquals(NOW + 2_000, AudienceSystem.windowEnd(text, NOW + 5_000, 4, 4, NOW));
  }

  @Test
  void movementKeepsItsEnd() {
    assertEquals(END, AudienceSystem.windowEnd(new AudienceContext.Move(), END, 3, 3, NOW));
  }
}
