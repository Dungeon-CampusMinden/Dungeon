package audience.relay;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * HTTP routes of the relay, all below {@code /play}.
 *
 * <p>The game creates a session, with the relay's create key if it has one, publishes its public
 * state, and long-polls the devices' inputs with the game token it received on creation. Devices
 * load the static page, join with the session code, stream the state as server-sent events, and
 * post inputs with their device token.
 */
final class RelayHttpServer implements AutoCloseable {
  private static final String PREFIX = "/play";
  private static final String GAME_SESSIONS = PREFIX + "/api/game/sessions";
  private static final String DEVICE_SESSIONS = PREFIX + "/api/sessions/";
  private static final Duration LONG_POLL = Duration.ofSeconds(25);
  private static final Duration HEARTBEAT = Duration.ofSeconds(15);
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Map<String, String> STATIC_FILES =
      Map.of(
          "index.html", "text/html; charset=utf-8",
          "app.js", "text/javascript; charset=utf-8",
          "style.css", "text/css; charset=utf-8",
          "fonts/Doto_Rounded-ExtraBold.ttf", "font/ttf",
          "fonts/Lexend-Regular.ttf", "font/ttf",
          "fonts/Lexend-Bold.ttf", "font/ttf");
  private static final String CONTENT_SECURITY_POLICY =
      "default-src 'self'; img-src 'self' data:; base-uri 'none'; form-action 'none';"
          + " frame-ancestors 'none'";

  private final RelayConfig config;
  private final HttpServer server;
  private final Map<String, RelaySession> sessions = new ConcurrentHashMap<>();
  private final ScheduledExecutorService sweeper =
      Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());

  RelayHttpServer(RelayConfig config) throws IOException {
    this.config = config;
    server = HttpServer.create(config.address(), 0);
    server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    server.createContext(PREFIX, this::handle);
  }

  void start() {
    server.start();
    sweeper.scheduleAtFixedRate(this::removeExpired, 15, 15, TimeUnit.SECONDS);
  }

  @Override
  public void close() {
    sweeper.shutdownNow();
    sessions.values().forEach(RelaySession::close);
    server.stop(1);
  }

  private void removeExpired() {
    Instant now = Instant.now();
    sessions
        .values()
        .removeIf(
            session -> {
              if (!session.expired(now, config.idleTimeout())) return false;
              session.close();
              return true;
            });
  }

  private void handle(HttpExchange exchange) throws IOException {
    try (exchange) {
      String path = exchange.getRequestURI().getPath();
      try {
        if (path.equals(PREFIX)) {
          exchange.getResponseHeaders().set("Location", PREFIX + "/");
          exchange.sendResponseHeaders(302, -1);
        } else if (path.equals(PREFIX + "/api/health")) {
          requireMethod(exchange, "GET");
          sendJson(exchange, 200, "{\"status\":\"ok\"}");
        } else if (path.equals(GAME_SESSIONS) || path.startsWith(GAME_SESSIONS + "/")) {
          game(exchange, path.substring(GAME_SESSIONS.length()));
        } else if (path.startsWith(DEVICE_SESSIONS)) {
          device(exchange, path.substring(DEVICE_SESSIONS.length()));
        } else {
          staticFile(exchange, path.substring(PREFIX.length()));
        }
      } catch (HttpError error) {
        sendError(exchange, error.status, error.getMessage());
      } catch (JacksonException | IllegalArgumentException exception) {
        sendError(exchange, 400, "Invalid request");
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
      } catch (IOException exception) {
        // The client went away, for example a device that closed its event stream.
      }
    }
  }

  // --- game API

  private void game(HttpExchange exchange, String rest) throws IOException, InterruptedException {
    if (rest.isEmpty()) {
      requireMethod(exchange, "POST");
      createSession(exchange);
      return;
    }
    String[] parts = rest.substring(1).split("/", -1);
    RelaySession session = session(parts[0]);
    if (!session.gameAuthorized(bearer(exchange))) throw new HttpError(401, "Unauthorized");
    String operation = parts.length > 1 ? parts[1] : "";
    switch (operation) {
      case "" -> {
        requireMethod(exchange, "DELETE");
        session.close();
        sessions.remove(session.code(), session);
        sendJson(exchange, 200, "{}");
      }
      case "state" -> {
        requireMethod(exchange, "PUT");
        JsonNode state =
            JsonMapper.shared().readTree(readBody(exchange, RelayConfig.MAX_BODY_BYTES));
        if (!state.isObject()) throw new HttpError(400, "State must be an object");
        session.state(JsonMapper.shared().writeValueAsString(state));
        sendJson(exchange, 200, "{}");
      }
      case "inbox" -> {
        requireMethod(exchange, "GET");
        long after = Long.parseLong(query(exchange).getOrDefault("after", "0"));
        List<ObjectNode> messages = session.awaitInbox(after, LONG_POLL);
        ArrayNode array = JsonMapper.shared().createArrayNode();
        messages.forEach(array::add);
        ObjectNode body = JsonMapper.shared().createObjectNode();
        body.set("messages", array);
        sendJson(exchange, 200, JsonMapper.shared().writeValueAsString(body));
      }
      default -> throw new HttpError(404, "Not found");
    }
  }

  private void createSession(HttpExchange exchange) throws IOException {
    if (config.createKey().isPresent()
        && !RelaySession.matches(config.createKey().get(), bearer(exchange))) {
      throw new HttpError(401, "Unauthorized");
    }
    JsonNode body = JsonMapper.shared().readTree(readBody(exchange, RelayConfig.MAX_BODY_BYTES));
    JsonNode surveys = body.path("surveys");
    if (!surveys.isObject()) surveys = JsonMapper.shared().createObjectNode();
    if (sessions.size() >= config.maxSessions()) throw new HttpError(503, "Too many sessions");
    String token = RelaySession.newToken();
    RelaySession session;
    do {
      String code = Integer.toString(100_000 + RANDOM.nextInt(900_000));
      session = new RelaySession(code, token, surveys, config.maxParticipants());
    } while (sessions.putIfAbsent(session.code(), session) != null);
    ObjectNode response =
        JsonMapper.shared().createObjectNode().put("code", session.code()).put("gameToken", token);
    sendJson(exchange, 201, JsonMapper.shared().writeValueAsString(response));
  }

  // --- device API

  private void device(HttpExchange exchange, String rest) throws IOException, InterruptedException {
    String[] parts = rest.split("/", -1);
    // Exact paths only, so that proxy rules for single routes such as join cannot be bypassed.
    if (parts.length != 2) throw new HttpError(404, "Not found");
    RelaySession session = session(parts[0]);
    switch (parts[1]) {
      case "join" -> {
        requireMethod(exchange, "POST");
        JsonNode body = JsonMapper.shared().readTree(readBody(exchange, 1024));
        Optional<String> token =
            Optional.ofNullable(body.get("token"))
                .filter(JsonNode::isString)
                .map(JsonNode::asString);
        RelaySession.Joined joined;
        try {
          joined = session.join(token);
        } catch (IllegalStateException exception) {
          throw new HttpError(409, exception.getMessage());
        }
        ObjectNode response =
            JsonMapper.shared()
                .createObjectNode()
                .put("token", joined.token())
                .put("pid", joined.pid());
        response.set("surveys", session.surveys());
        sendJson(exchange, 200, JsonMapper.shared().writeValueAsString(response));
      }
      case "events" -> {
        requireMethod(exchange, "GET");
        stream(exchange, session, deviceId(session, query(exchange).get("token")));
      }
      case "input" -> {
        requireMethod(exchange, "POST");
        JsonNode body =
            JsonMapper.shared().readTree(readBody(exchange, RelayConfig.MAX_INPUT_BYTES));
        String pid = deviceId(session, body.path("token").asString(null));
        boolean queued;
        try {
          queued = session.input(pid, input(body));
        } catch (IllegalStateException exception) {
          throw new HttpError(409, exception.getMessage());
        }
        if (!queued) throw new HttpError(429, "Too many inputs");
        sendJson(exchange, 200, "{}");
      }
      default -> throw new HttpError(404, "Not found");
    }
  }

  // Copies the fields of a device input that the game reads; everything else, including the token,
  // stays at the relay.
  private static ObjectNode input(JsonNode body) {
    String type = body.path("type").asString("");
    ObjectNode message = JsonMapper.shared().createObjectNode().put("type", type);
    switch (type) {
      case "vote" -> {
        if (!body.path("window").canConvertToLong() || !body.has("value")) {
          throw new HttpError(400, "A vote needs window and value");
        }
        message.put("window", body.get("window").asLong());
        message.set("value", body.get("value"));
      }
      case "survey" -> {
        if (!body.path("phase").isString() || !body.path("answers").isObject()) {
          throw new HttpError(400, "A survey needs phase and answers");
        }
        message.put("phase", body.get("phase").asString());
        message.set("answers", body.get("answers"));
      }
      default -> throw new HttpError(400, "Unknown input type");
    }
    return message;
  }

  // Streams every state change as a server-sent event until the device or session goes away, or the
  // device opens a newer stream; then a "replaced" event tells this page not to reconnect. Each
  // event carries the state and its age, so a device that connects late still counts timers from
  // the moment the game published them.
  private static void stream(HttpExchange exchange, RelaySession session, String pid)
      throws IOException, InterruptedException {
    long id = session.openStream(pid);
    exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
    exchange.getResponseHeaders().set("Cache-Control", "no-store");
    exchange.getResponseHeaders().set("X-Accel-Buffering", "no");
    exchange.sendResponseHeaders(200, 0);
    OutputStream out = exchange.getResponseBody();
    long seen = 0;
    while (true) {
      Optional<RelaySession.State> state;
      try {
        state = session.awaitState(seen, HEARTBEAT, pid, id);
      } catch (RelaySession.StreamReplacedException exception) {
        out.write("event: replaced\ndata: {}\n\n".getBytes(StandardCharsets.UTF_8));
        out.flush();
        return;
      }
      if (state.isPresent()) {
        seen = state.get().version();
        String data =
            "{\"ageMs\":" + state.get().ageMs() + ",\"state\":" + state.get().json() + "}";
        out.write(("event: state\ndata: " + data + "\n\n").getBytes(StandardCharsets.UTF_8));
      } else {
        out.write(": ping\n\n".getBytes(StandardCharsets.UTF_8));
      }
      out.flush();
      if (state.isPresent() && state.get().closed()) return;
    }
  }

  // --- static page

  private static void staticFile(HttpExchange exchange, String path) throws IOException {
    requireMethod(exchange, "GET");
    String name = path.equals("/") ? "index.html" : path.substring(1);
    String contentType = STATIC_FILES.get(name);
    if (contentType == null) throw new HttpError(404, "Not found");
    byte[] bytes;
    try (InputStream in =
        RelayHttpServer.class.getResourceAsStream("/audience/relay/web/" + name)) {
      if (in == null) throw new HttpError(404, "Not found");
      bytes = in.readAllBytes();
    }
    exchange.getResponseHeaders().set("Content-Type", contentType);
    // Fonts never change in place; the page itself must update with every deployment.
    exchange
        .getResponseHeaders()
        .set("Cache-Control", name.startsWith("fonts/") ? "public, max-age=86400" : "no-cache");
    exchange.getResponseHeaders().set("Content-Security-Policy", CONTENT_SECURITY_POLICY);
    exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
    exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
    exchange.sendResponseHeaders(200, bytes.length);
    exchange.getResponseBody().write(bytes);
  }

  // --- helpers

  private RelaySession session(String code) {
    RelaySession session = sessions.get(code);
    if (session == null) throw new HttpError(404, "Unknown session");
    return session;
  }

  private static String bearer(HttpExchange exchange) {
    String authorization = exchange.getRequestHeaders().getFirst("Authorization");
    return authorization != null && authorization.startsWith("Bearer ")
        ? authorization.substring("Bearer ".length())
        : null;
  }

  private static String deviceId(RelaySession session, String token) {
    return session.pid(token).orElseThrow(() -> new HttpError(401, "Unknown device"));
  }

  private static void requireMethod(HttpExchange exchange, String method) {
    if (!exchange.getRequestMethod().equals(method)) {
      exchange.getResponseHeaders().set("Allow", method);
      throw new HttpError(405, "Method not allowed");
    }
  }

  private static String readBody(HttpExchange exchange, int limit) throws IOException {
    byte[] body = exchange.getRequestBody().readNBytes(limit + 1);
    if (body.length > limit) throw new HttpError(413, "Request body is too large");
    return new String(body, StandardCharsets.UTF_8);
  }

  private static Map<String, String> query(HttpExchange exchange) {
    Map<String, String> values = new HashMap<>();
    String query = exchange.getRequestURI().getRawQuery();
    if (query == null) return values;
    for (String pair : query.split("&")) {
      int split = pair.indexOf('=');
      if (split <= 0) continue;
      values.put(
          URLDecoder.decode(pair.substring(0, split), StandardCharsets.UTF_8),
          URLDecoder.decode(pair.substring(split + 1), StandardCharsets.UTF_8));
    }
    return values;
  }

  private static void sendError(HttpExchange exchange, int status, String message)
      throws IOException {
    ObjectNode body = JsonMapper.shared().createObjectNode().put("error", message);
    sendJson(exchange, status, JsonMapper.shared().writeValueAsString(body));
  }

  private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    exchange.getResponseHeaders().set("Cache-Control", "no-store");
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
  }

  /** Ends a request with an HTTP status and a JSON error message. */
  private static final class HttpError extends RuntimeException {
    private final int status;

    HttpError(int status, String message) {
      super(message, null, false, false);
      this.status = status;
    }
  }
}
