package engine.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tracking.core.TrackingJson;
import tracking.core.TrackingSessionDescriptor;

/** Verifies optional playthrough metadata in the tracking session header. */
class TrackingRunIdMetadataTest {

  @Test
  void roundTripsOptionalRunIdInSessionMetadata() {
    UUID runId = UUID.randomUUID();
    TrackingSessionDescriptor descriptor =
        new TrackingSessionDescriptor(
            1, UUID.randomUUID(), "system-recovery", Instant.parse("2026-09-22T08:25:32Z"),
            Optional.of(runId));

    TrackingSessionDescriptor restored =
        TrackingJson.read(TrackingJson.write(descriptor), TrackingSessionDescriptor.class);

    assertEquals(Optional.of(runId), restored.runId());
  }

  @Test
  void keepsLegacyDescriptorWithoutRunIdValid() {
    TrackingSessionDescriptor descriptor =
        new TrackingSessionDescriptor(
            1, UUID.randomUUID(), "system-recovery", Instant.parse("2026-09-22T08:25:32Z"));

    assertEquals(Optional.empty(), descriptor.runId());
  }
}
