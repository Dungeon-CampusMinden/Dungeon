package engine.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tracking.core.TrackingEvent;
import tracking.core.TrackingEventType;
import tracking.core.TrackingInteractionStatus;
import tracking.core.TrackingJson;

class TrackingInteractionTest {
  @TempDir Path directory;

  @Test
  void interactionDoesNotConsumeAnswerNumberOrActivatePuzzle() throws Exception {
    TrackingSession session =
        new TrackingSession(
            new TrackingConfig(
                "system-recovery",
                URI.create("http://127.0.0.1:8088"),
                Optional.empty(),
                directory,
                "tracking@example.com",
                Optional.empty()));
    UUID participant = UUID.randomUUID();

    TrackingEvent interaction =
        session.interaction(
            "bubble-sort",
            "sort-machine",
            "insert",
            TrackingInteractionStatus.BLOCKED,
            "missing-program",
            participant);
    assertEquals(TrackingEventType.INTERACTION_RECORDED, interaction.eventType());
    assertFalse(interaction.outcome().isPresent());
    assertFalse(session.currentPuzzleId().isPresent());
    assertEquals(
        interaction, TrackingJson.read(TrackingJson.write(interaction), TrackingEvent.class));

    TrackingEvent answer =
        session.attempt(
            "bubble-sort",
            "sort-program",
            "source",
            "wrong code",
            false,
            participant,
            Optional.empty());
    assertEquals(1, answer.payload().get("attemptNumber").intValue());
    var records = Files.readAllLines(session.outboxPath());
    assertEquals(3, records.size());
    assertTrue(records.get(1).contains("\"eventType\":\"INTERACTION_RECORDED\""));
  }

  @Test
  void interactionRejectsMissingReason() {
    TrackingSession session =
        new TrackingSession(
            new TrackingConfig(
                "system-recovery",
                URI.create("http://127.0.0.1:8088"),
                Optional.empty(),
                directory,
                "tracking@example.com",
                Optional.empty()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            session.interaction(
                "bubble-sort",
                "sort-machine",
                "insert",
                TrackingInteractionStatus.BLOCKED,
                "",
                UUID.randomUUID()));
  }
}
