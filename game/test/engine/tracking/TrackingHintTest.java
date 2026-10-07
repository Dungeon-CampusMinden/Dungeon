package engine.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tracking.core.TrackingEvent;
import tracking.core.TrackingEventType;

class TrackingHintTest {
  @TempDir Path directory;

  @Test
  void onlyAutomaticHintsCarryATriggerInThePayload() {
    TrackingSession session =
        new TrackingSession(
            new TrackingConfig(
                "system-recovery",
                URI.create("http://127.0.0.1:8088"),
                Optional.empty(),
                directory,
                "tracking@example.com",
                UUID.randomUUID()));
    UUID participant = UUID.randomUUID();

    TrackingEvent requested =
        session.hintUsed("energy-array", "energy-array:orientation", participant).orElseThrow();
    TrackingEvent automatic =
        session.hintUsed("energy-array", "energy-array:approach", participant, true).orElseThrow();

    assertEquals(TrackingEventType.HINT_USED, automatic.eventType());
    assertEquals(participant, automatic.participantId().orElseThrow());
    assertEquals("automatic", automatic.payload().get("trigger").asString());
    assertFalse(requested.payload().has("trigger"));
  }
}
