package engine.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.network.codec.converters.s2c.DialogFeedbackConverter;
import engine.network.messages.s2c.DialogFeedbackMessage;
import org.junit.jupiter.api.Test;

/** Verifies transport of targeted live dialog feedback. */
class DialogFeedbackConverterTest {

  private static final DialogFeedbackConverter CONVERTER = new DialogFeedbackConverter();

  @Test
  void roundTripPreservesTargetAndLocalizedStatus() {
    DialogFeedbackMessage message =
        new DialogFeedbackMessage(
            "dialog-terminal", "system-core-meta", "abc123", "computer.feedback-correct", true);

    var proto = CONVERTER.toProto(message);
    DialogFeedbackMessage roundTrip = CONVERTER.fromProto(proto);

    assertEquals(message, roundTrip);
  }
}
