package engine.tracking;

import engine.utils.logging.DungeonLogger;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tracking.core.TrackingJson;
import tracking.core.TrackingJsonlReadResult;
import tracking.core.TrackingOutboxUpload;

/**
 * Uploads the outboxes that earlier sessions left behind.
 *
 * <p>An outbox stays behind when its session ended without a confirmed upload, for example because
 * the backend was unreachable or the game was killed. The backend accepts repeated data, so every
 * outbox is uploaded as a whole. One whose finish the backend confirms is deleted; one without a
 * finish record stays, because its session never ended properly.
 */
final class TrackingResend {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TrackingResend.class);
  private static final int BATCH_SIZE = 100;
  private static final AtomicBoolean STARTED = new AtomicBoolean();

  private TrackingResend() {}

  /**
   * Starts the single background pass of this process. A session calls it before creating its own
   * outbox, so the pass only sees files of earlier sessions.
   *
   * @param config configuration of the starting session; names the outbox directory and backend
   */
  static void startOnce(TrackingConfig config) {
    if (!STARTED.compareAndSet(false, true) || !Files.isDirectory(config.outboxDirectory())) {
      return;
    }
    List<Path> outboxes;
    try (var files = Files.list(config.outboxDirectory())) {
      outboxes = files.filter(Tracking::isTrackingFile).toList();
    } catch (IOException | RuntimeException exception) {
      LOGGER.warn("Could not list tracking outboxes in {}", config.outboxDirectory(), exception);
      return;
    }
    if (!outboxes.isEmpty()) {
      Thread.ofPlatform()
          .daemon()
          .name("tracking-outbox-resend")
          .start(() -> resend(config, outboxes));
    }
  }

  private static void resend(TrackingConfig config, List<Path> outboxes) {
    HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    for (Path outbox : outboxes) {
      try {
        TrackingJsonlReadResult records = TrackingJson.readJsonlRecoveringTruncatedTail(outbox);
        TrackingOutboxUpload.upload(
            client, config.endpoint(), config.apiKey(), records, BATCH_SIZE);
        if (records.finish().isPresent()) {
          Files.deleteIfExists(outbox);
          LOGGER.info("Uploaded and removed earlier tracking outbox {}", outbox);
        }
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        return;
      } catch (IOException | RuntimeException exception) {
        // Connection failures carry no message, so the log names the exception itself.
        LOGGER.warn(
            "Could not upload earlier tracking outbox {}: {}", outbox, exception.toString());
      }
    }
  }
}
