package engine.tracking;

import engine.utils.logging.DungeonLogger;
import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import tracking.core.TrackingJson;
import tracking.core.TrackingJsonlReadResult;
import tracking.core.TrackingOutboxUpload;

/**
 * Resends earlier outboxes of sessions that uploaded to the endpoint built into the game and
 * deletes only those with an acknowledged finish.
 */
final class TrackingResend {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TrackingResend.class);
  private static final int BATCH_SIZE = 100;
  private static final AtomicBoolean STARTED = new AtomicBoolean();

  private TrackingResend() {}

  /**
   * Starts the single background pass of this process. A session that uploads to the built-in
   * endpoint calls it before creating its own outbox, so the pass only sees files of earlier
   * sessions.
   *
   * @param config configuration of the starting session; names the outbox directory and backend
   */
  static void startOnce(TrackingConfig config) {
    if (!STARTED.compareAndSet(false, true) || !Files.isDirectory(config.outboxDirectory())) {
      return;
    }
    List<Path> outboxes;
    try (var files = Files.list(config.outboxDirectory())) {
      outboxes = files.filter(TrackingResend::isCentralOutbox).toList();
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

  private static boolean isCentralOutbox(Path file) {
    return Files.isRegularFile(file)
        && file.getFileName().toString().endsWith(TrackingOutbox.CENTRAL_SUFFIX);
  }

  /**
   * Uploads each outbox completely and deletes it once the backend acknowledged its finish.
   *
   * @param config names the backend
   * @param outboxes outbox files of earlier sessions
   */
  static void resend(TrackingConfig config, List<Path> outboxes) {
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
      } catch (ConnectException | HttpConnectTimeoutException exception) {
        LOGGER.warn("Tracking backend is unreachable; earlier outboxes stay for the next start");
        return;
      } catch (IOException | RuntimeException exception) {
        // Connection failures carry no message, so the log names the exception itself.
        LOGGER.warn(
            "Could not upload earlier tracking outbox {}: {}", outbox, exception.toString());
      }
    }
  }
}
