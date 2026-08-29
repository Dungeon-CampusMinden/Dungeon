package engine.tracking;

import engine.utils.logging.DungeonLogger;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import tracking.core.TrackingAck;
import tracking.core.TrackingBatch;
import tracking.core.TrackingEvent;
import tracking.core.TrackingJson;
import tracking.core.TrackingParticipant;
import tracking.core.TrackingSessionDescriptor;
import tracking.core.TrackingSessionFinish;

/** Ordered best-effort HTTP uploader. Its executor owns all mutable upload state. */
final class TrackingUploader {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TrackingUploader.class);
  private static final int BATCH_SIZE = 100;
  private static final long MAX_BACKOFF_SECONDS = 30;
  private static final Duration ROUTINE_REQUEST_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration SHUTDOWN_GRACE = Duration.ofMillis(250);

  private final TrackingConfig config;
  private final TrackingSessionDescriptor descriptor;
  private final HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
  private final ScheduledExecutorService executor =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "tracking-http-uploader");
            thread.setDaemon(true);
            return thread;
          });
  private final List<TrackingEvent> pending = new ArrayList<>();
  private final AtomicLong acknowledgedSequence = new AtomicLong();
  private final AtomicBoolean finishAcknowledged = new AtomicBoolean();

  private TrackingSessionFinish finish;
  private List<TrackingParticipant> participants = List.of();
  private long finalSequence;
  private boolean uploadScheduled;
  private boolean finalizing;
  private ScheduledFuture<?> scheduledUpload;
  private int failures;

  TrackingUploader(TrackingConfig config, TrackingSessionDescriptor descriptor) {
    this.config = config;
    this.descriptor = descriptor;
    executor.execute(this::refreshAckAndUpload);
  }

  void offer(TrackingBatch batch) {
    executor.execute(
        () -> {
          participants = batch.participants();
          pending.addAll(batch.events());
          scheduleUpload(0);
        });
  }

  void finishAndFlush(TrackingSessionFinish finish, long finalSequence, Duration timeout) {
    long deadlineNanos = System.nanoTime() + timeout.toNanos();
    CountDownLatch completed = new CountDownLatch(1);
    executor.execute(
        () -> {
          this.finish = finish;
          this.finalSequence = finalSequence;
          finalizing = true;
          if (scheduledUpload != null) {
            scheduledUpload.cancel(false);
          }
          uploadScheduled = false;
          try {
            uploadFinal(deadlineNanos);
          } finally {
            completed.countDown();
          }
        });
    try {
      completed.await(remaining(deadlineNanos).toNanos(), TimeUnit.NANOSECONDS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    } finally {
      executor.shutdownNow();
      try {
        if (!executor.awaitTermination(SHUTDOWN_GRACE.toMillis(), TimeUnit.MILLISECONDS)) {
          LOGGER.warn("Tracking uploader did not stop promptly after its final attempt");
        }
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
      }
    }
  }

  boolean pending(long latestSequence, boolean sessionFinished) {
    return acknowledgedSequence.get() < latestSequence
        || (sessionFinished && !finishAcknowledged.get());
  }

  private void refreshAckAndUpload() {
    try {
      HttpResponse<String> response = send("GET", endpoint("ack"), null);
      if (response.statusCode() == 200) {
        TrackingAck ack = TrackingJson.read(response.body(), TrackingAck.class);
        if (ack.sessionId().equals(descriptor.sessionId())) {
          acknowledgedSequence.accumulateAndGet(ack.lastPersistedSequence(), Math::max);
        }
      }
    } catch (IOException | InterruptedException | RuntimeException exception) {
      if (exception instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      LOGGER.debug("Could not read initial tracking acknowledgement: {}", exception.getMessage());
    }
    scheduleUpload(0);
  }

  private void scheduleUpload(long delaySeconds) {
    if (uploadScheduled) {
      return;
    }
    uploadScheduled = true;
    scheduledUpload =
        executor.schedule(
            () -> {
              uploadScheduled = false;
              upload();
            },
            delaySeconds,
            TimeUnit.SECONDS);
  }

  private void upload() {
    pending.removeIf(event -> event.sessionSequence() <= acknowledgedSequence.get());
    if (!pending.isEmpty()) {
      int count = Math.min(BATCH_SIZE, pending.size());
      List<TrackingEvent> events = List.copyOf(pending.subList(0, count));
      TrackingBatch batch =
          new TrackingBatch(descriptor.schemaVersion(), descriptor, participants, events);
      try {
        HttpResponse<String> response = send("POST", endpoint("events"), TrackingJson.write(batch));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
          retry("event upload returned HTTP " + response.statusCode());
          return;
        }
        TrackingAck ack = TrackingJson.read(response.body(), TrackingAck.class);
        if (!ack.sessionId().equals(descriptor.sessionId())) {
          retry("event upload returned an acknowledgement for another session");
          return;
        }
        acknowledgedSequence.accumulateAndGet(ack.lastPersistedSequence(), Math::max);
        failures = 0;
        scheduleUpload(0);
        return;
      } catch (IOException | InterruptedException | RuntimeException exception) {
        if (exception instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        retry("event upload failed: " + exception.getMessage());
        return;
      }
    }

    if (finish != null
        && acknowledgedSequence.get() >= finalSequence
        && !finishAcknowledged.get()) {
      try {
        HttpResponse<String> response =
            send("POST", endpoint("finish"), TrackingJson.write(finish));
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
          if (acknowledgesFinish(response.body())) {
            finishAcknowledged.set(true);
            executor.shutdown();
            return;
          }
          retry("finish upload returned an invalid acknowledgement");
          return;
        }
        retry("finish upload returned HTTP " + response.statusCode());
      } catch (IOException | InterruptedException | RuntimeException exception) {
        if (exception instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        retry("finish upload failed: " + exception.getMessage());
      }
    }
  }

  private void retry(String reason) {
    if (finalizing) {
      LOGGER.warn("{}; final upload attempt ended", reason);
      return;
    }
    failures++;
    long delay = Math.min(MAX_BACKOFF_SECONDS, 1L << Math.min(failures - 1, 5));
    LOGGER.warn("{}; retrying in {} s", reason, delay);
    scheduleUpload(delay);
  }

  private void uploadFinal(long deadlineNanos) {
    while (System.nanoTime() < deadlineNanos) {
      pending.removeIf(event -> event.sessionSequence() <= acknowledgedSequence.get());
      if (!pending.isEmpty()) {
        int count = Math.min(BATCH_SIZE, pending.size());
        List<TrackingEvent> events = List.copyOf(pending.subList(0, count));
        TrackingBatch batch =
            new TrackingBatch(descriptor.schemaVersion(), descriptor, participants, events);
        try {
          HttpResponse<String> response =
              send("POST", endpoint("events"), TrackingJson.write(batch), remaining(deadlineNanos));
          if (response.statusCode() < 200 || response.statusCode() >= 300) {
            LOGGER.warn("Final tracking upload returned HTTP {}", response.statusCode());
            return;
          }
          TrackingAck ack = TrackingJson.read(response.body(), TrackingAck.class);
          if (!ack.sessionId().equals(descriptor.sessionId())) {
            LOGGER.warn("Final tracking upload acknowledged another session");
            return;
          }
          acknowledgedSequence.accumulateAndGet(ack.lastPersistedSequence(), Math::max);
          continue;
        } catch (IOException | InterruptedException | RuntimeException exception) {
          if (exception instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          LOGGER.warn("Final tracking upload failed: {}", exception.getMessage());
          return;
        }
      }

      if (acknowledgedSequence.get() < finalSequence || finishAcknowledged.get()) {
        return;
      }
      try {
        HttpResponse<String> response =
            send("POST", endpoint("finish"), TrackingJson.write(finish), remaining(deadlineNanos));
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
          if (acknowledgesFinish(response.body())) {
            finishAcknowledged.set(true);
          } else {
            LOGGER.warn("Final tracking finish upload returned an invalid acknowledgement");
          }
        } else {
          LOGGER.warn("Final tracking finish upload returned HTTP {}", response.statusCode());
        }
      } catch (IOException | InterruptedException | RuntimeException exception) {
        if (exception instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        LOGGER.warn("Final tracking finish upload failed: {}", exception.getMessage());
      }
      return;
    }
  }

  private boolean acknowledgesFinish(String responseBody) {
    TrackingAck ack = TrackingJson.read(responseBody, TrackingAck.class);
    return ack.sessionId().equals(descriptor.sessionId())
        && ack.lastPersistedSequence() >= finalSequence;
  }

  private HttpResponse<String> send(String method, URI uri, String body)
      throws IOException, InterruptedException {
    return send(method, uri, body, ROUTINE_REQUEST_TIMEOUT);
  }

  private HttpResponse<String> send(String method, URI uri, String body, Duration timeout)
      throws IOException, InterruptedException {
    HttpRequest.Builder request =
        HttpRequest.newBuilder(uri).timeout(timeout).header("Accept", "application/json");
    config.apiKey().ifPresent(key -> request.header("Authorization", "Bearer " + key));
    if (body == null) {
      request.method(method, HttpRequest.BodyPublishers.noBody());
    } else {
      request
          .header("Content-Type", "application/json")
          .method(method, HttpRequest.BodyPublishers.ofString(body));
    }
    return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
  }

  private static Duration remaining(long deadlineNanos) {
    return Duration.ofNanos(Math.max(1, deadlineNanos - System.nanoTime()));
  }

  private URI endpoint(String operation) {
    String base = config.endpoint().orElseThrow().toString().replaceAll("/+$", "");
    String session = "/tracking/sessions/" + descriptor.sessionId();
    return URI.create(base + session + "/" + operation);
  }
}
