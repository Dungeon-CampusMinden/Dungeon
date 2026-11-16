package engine.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tracking.core.TrackingAck;
import tracking.core.TrackingJson;
import tracking.core.TrackingSessionDescriptor;
import tracking.core.TrackingSessionFinish;
import tracking.core.TrackingSessionStatus;

/** Verifies that the resend deletes an outbox only after the backend acknowledged its finish. */
class TrackingResendTest {
  @TempDir Path directory;

  @Test
  void deletesOnlyOutboxesWithAcknowledgedFinish() throws Exception {
    TrackingOutbox finished = outbox(true);
    TrackingOutbox unfinished = outbox(false);
    TrackingOutbox rejected = outbox(true);
    String rejectedFinish = "/tracking/sessions/" + sessionId(rejected) + "/finish";
    List<String> requests = new CopyOnWriteArrayList<>();
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          String path = exchange.getRequestURI().getPath();
          requests.add(path);
          UUID sessionId = UUID.fromString(path.split("/")[3]);
          byte[] body =
              TrackingJson.write(new TrackingAck(1, sessionId, 0, 0))
                  .getBytes(StandardCharsets.UTF_8);
          exchange.sendResponseHeaders(path.equals(rejectedFinish) ? 500 : 200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    server.start();
    try {
      TrackingConfig config =
          new TrackingConfig(
              "system-recovery",
              URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
              Optional.empty(),
              directory,
              "tracking@example.com",
              UUID.randomUUID());

      TrackingResend.resend(config, List.of(finished.path(), unfinished.path(), rejected.path()));
    } finally {
      server.stop(0);
    }

    assertFalse(Files.exists(finished.path()));
    assertTrue(requests.contains("/tracking/sessions/" + sessionId(finished) + "/finish"));
    assertTrue(Files.exists(unfinished.path()));
    assertTrue(Files.exists(rejected.path()));
    assertEquals(5, requests.size());
  }

  private TrackingOutbox outbox(boolean withFinish) {
    UUID sessionId = UUID.randomUUID();
    TrackingOutbox outbox =
        TrackingOutbox.create(
            directory,
            new TrackingSessionDescriptor(
                1, sessionId, "system-recovery", Instant.now(), UUID.randomUUID(), 0),
            true);
    if (withFinish) {
      outbox.append(
          new TrackingSessionFinish(
              1,
              sessionId,
              0,
              TrackingSessionStatus.COMPLETED,
              Instant.now(),
              0,
              Optional.empty()));
    }
    return outbox;
  }

  private static String sessionId(TrackingOutbox outbox) {
    return outbox.path().getFileName().toString().replace(TrackingOutbox.CENTRAL_SUFFIX, "");
  }
}
