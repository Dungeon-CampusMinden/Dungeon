package engine.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tracking.core.TrackingJson;
import tracking.core.TrackingSessionDescriptor;

/** Verifies mandatory playthrough identity and resumed time in the session header. */
class TrackingRunIdMetadataTest {

  @Test
  void roundTripsRunIdAndResumedTimeInSessionMetadata() {
    UUID runId = UUID.randomUUID();
    TrackingSessionDescriptor descriptor =
        new TrackingSessionDescriptor(
            1,
            UUID.randomUUID(),
            "system-recovery",
            Instant.parse("2026-09-22T08:25:32Z"),
            runId,
            42_000);

    TrackingSessionDescriptor restored =
        TrackingJson.read(TrackingJson.write(descriptor), TrackingSessionDescriptor.class);

    assertEquals(runId, restored.runId());
    assertEquals(42_000, restored.resumedAtActiveMs());
  }
}
