package engine.tracking;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies that privacy deletion removes tracking files without touching unrelated data. */
class TrackingLocalDataTest {

  @TempDir Path temporaryDirectory;

  @AfterEach
  void restoreTrackingDefaults() {
    System.clearProperty(TrackingConfig.OUTBOX_PROPERTY);
    Tracking.configureRoom("tracking-test-cleanup", Optional.empty(), true);
  }

  @Test
  void deletesJsonlTrackingFilesOnly() throws Exception {
    Path outbox = temporaryDirectory.resolve("tracking-outbox");
    Files.createDirectories(outbox);
    Path trackingFile = outbox.resolve(UUID.randomUUID() + ".jsonl");
    Path unrelatedFile = outbox.resolve("keep-me.txt");
    Files.writeString(trackingFile, "tracking");
    Files.writeString(unrelatedFile, "keep");

    System.setProperty(TrackingConfig.OUTBOX_PROPERTY, outbox.toString());
    Tracking.configureRoom("system-recovery", "amatutat@hsbi.de", Optional.empty(), true);

    assertTrue(Tracking.deleteLocalData());
    assertFalse(Files.exists(trackingFile));
    assertTrue(Files.exists(unrelatedFile));
  }
}
