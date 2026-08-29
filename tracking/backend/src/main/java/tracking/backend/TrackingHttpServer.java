package tracking.backend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import tracking.backend.TrackingRepository.RepositoryConflictException;
import tracking.core.TrackingAck;
import tracking.core.TrackingBatch;
import tracking.core.TrackingJson;
import tracking.core.TrackingSessionFinish;

final class TrackingHttpServer implements AutoCloseable {
  private static final String SESSIONS_PATH = "/tracking/sessions/";

  private final BackendConfig config;
  private final TrackingRepository repository;
  private final HttpServer server;

  TrackingHttpServer(final BackendConfig config, final TrackingRepository repository)
      throws IOException {
    this.config = config;
    this.repository = repository;
    server = HttpServer.create(config.address(), 0);
    server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    server.createContext("/health", this::health);
    server.createContext(SESSIONS_PATH, this::sessions);
  }

  void start() {
    server.start();
  }

  @Override
  public void close() {
    server.stop(1);
  }

  private void health(final HttpExchange exchange) throws IOException {
    try (exchange) {
      if (!exchange.getRequestMethod().equals("GET")) {
        methodNotAllowed(exchange, "GET");
        return;
      }
      if (repository.healthy()) {
        send(exchange, 200, "{\"status\":\"ok\"}");
      } else {
        send(exchange, 503, "{\"status\":\"unavailable\"}");
      }
    }
  }

  private void sessions(final HttpExchange exchange) throws IOException {
    try (exchange) {
      if (!authorized(exchange)) {
        exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
        sendError(exchange, 401, "Unauthorized");
        return;
      }
      try {
        Route route = route(exchange.getRequestURI().getPath());
        switch (route.operation()) {
          case "events" -> ingest(exchange, route.sessionId());
          case "ack" -> ack(exchange, route.sessionId());
          case "finish" -> finish(exchange, route.sessionId());
          default -> sendError(exchange, 404, "Not found");
        }
      } catch (IllegalArgumentException exception) {
        sendError(exchange, 400, exception.getMessage());
      } catch (NoSuchElementException exception) {
        sendError(exchange, 404, exception.getMessage());
      } catch (RepositoryConflictException exception) {
        sendError(exchange, 409, exception.getMessage());
      } catch (SQLException exception) {
        sendError(exchange, 503, "Tracking database is unavailable");
      } catch (RuntimeException exception) {
        sendError(exchange, 500, "Tracking request failed");
      }
    }
  }

  private void ingest(final HttpExchange exchange, final UUID sessionId)
      throws IOException, SQLException {
    if (!exchange.getRequestMethod().equals("POST")) {
      methodNotAllowed(exchange, "POST");
      return;
    }
    TrackingBatch batch = TrackingJson.read(readBody(exchange), TrackingBatch.class);
    if (!batch.session().sessionId().equals(sessionId)) {
      throw new IllegalArgumentException("Path and batch sessionId do not match");
    }
    if (batch.events().size() > config.maxBatchEvents()) {
      throw new IllegalArgumentException("Batch contains too many events");
    }
    send(exchange, 200, TrackingJson.write(repository.ingest(batch)));
  }

  private void ack(final HttpExchange exchange, final UUID sessionId)
      throws IOException, SQLException {
    if (!exchange.getRequestMethod().equals("GET")) {
      methodNotAllowed(exchange, "GET");
      return;
    }
    send(exchange, 200, TrackingJson.write(repository.ack(sessionId)));
  }

  private void finish(final HttpExchange exchange, final UUID sessionId)
      throws IOException, SQLException {
    if (!exchange.getRequestMethod().equals("POST")) {
      methodNotAllowed(exchange, "POST");
      return;
    }
    TrackingSessionFinish finish =
        TrackingJson.read(readBody(exchange), TrackingSessionFinish.class);
    if (!finish.sessionId().equals(sessionId)) {
      throw new IllegalArgumentException("Path and finish sessionId do not match");
    }
    TrackingAck result = repository.finish(finish);
    send(exchange, 200, TrackingJson.write(result));
  }

  private String readBody(final HttpExchange exchange) throws IOException {
    byte[] body = exchange.getRequestBody().readNBytes(config.maxBodyBytes() + 1);
    if (body.length > config.maxBodyBytes()) {
      throw new IllegalArgumentException("Request body is too large");
    }
    try {
      return StandardCharsets.UTF_8
          .newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(body))
          .toString();
    } catch (CharacterCodingException exception) {
      throw new IllegalArgumentException("Request body must be valid UTF-8", exception);
    }
  }

  private boolean authorized(final HttpExchange exchange) {
    Optional<String> expected = config.apiKey();
    if (expected.isEmpty()) {
      return true;
    }
    String authorization = exchange.getRequestHeaders().getFirst("Authorization");
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return false;
    }
    byte[] expectedBytes = expected.orElseThrow().getBytes(StandardCharsets.UTF_8);
    byte[] actualBytes =
        authorization.substring("Bearer ".length()).getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(expectedBytes, actualBytes);
  }

  private static Route route(final String path) {
    if (!path.startsWith(SESSIONS_PATH)) {
      throw new IllegalArgumentException("Invalid tracking path");
    }
    String[] segments = path.substring(SESSIONS_PATH.length()).split("/", -1);
    if (segments.length != 2 || segments[0].isBlank() || segments[1].isBlank()) {
      throw new IllegalArgumentException("Expected /tracking/sessions/{sessionId}/{operation}");
    }
    return new Route(UUID.fromString(segments[0]), segments[1]);
  }

  private static void methodNotAllowed(final HttpExchange exchange, final String allowed)
      throws IOException {
    exchange.getResponseHeaders().set("Allow", allowed);
    sendError(exchange, 405, "Method not allowed");
  }

  private static void sendError(final HttpExchange exchange, final int status, final String message)
      throws IOException {
    var body = TrackingJson.object();
    body.put("error", message == null ? "Invalid request" : message);
    send(exchange, status, body.toString());
  }

  private static void send(final HttpExchange exchange, final int status, final String body)
      throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    exchange.getResponseHeaders().set("Cache-Control", "no-store");
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
  }

  private record Route(UUID sessionId, String operation) {}
}
