package feature.audience;

import engine.utils.logging.DungeonLogger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import tools.jackson.databind.JsonNode;
import tracking.core.TrackingJson;

/**
 * Connection of the game server to the relay.
 *
 * <p>It creates the session, publishes the public state whenever the game calls {@link #publish},
 * and long-polls the devices' inputs on a background thread. The game thread takes the inputs with
 * {@link #drain(int)}; nothing here touches game state.
 */
final class AudienceRelayClient {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AudienceRelayClient.class);
  private static final Duration RETRY = Duration.ofSeconds(2);

  private final URI relay;
  private final Optional<String> key;
  private final String surveys;
  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  private final Queue<JsonNode> inbox = new ConcurrentLinkedQueue<>();
  private final AtomicReference<Session> session = new AtomicReference<>();
  private final AtomicReference<String> pendingState = new AtomicReference<>();
  private volatile boolean online = true;
  private volatile boolean closed;
  private boolean publishing;

  /**
   * Starts connecting in the background.
   *
   * @param relay base URL of the relay
   * @param key key the relay requires to create a session, if it requires one
   * @param surveys JSON object with the survey definitions the devices show, keyed by phase
   */
  AudienceRelayClient(URI relay, Optional<String> key, JsonNode surveys) {
    this.relay = relay;
    this.key = key;
    this.surveys = TrackingJson.object().set("surveys", surveys).toString();
    Thread.ofVirtual().name("audience-relay").start(this::run);
    // Quitting the game also ends the session, so devices do not wait for a game that is gone.
    Runtime.getRuntime().addShutdownHook(new Thread(this::close, "audience-relay-close"));
  }

  /**
   * @return the session code once the relay created the session
   */
  Optional<String> code() {
    return Optional.ofNullable(session.get()).map(Session::code);
  }

  /**
   * @return whether the last request to the relay succeeded
   */
  boolean online() {
    return online;
  }

  /**
   * @param limit most inputs to return
   * @return inputs received since the last call, in arrival order, at most {@code limit}
   */
  List<JsonNode> drain(int limit) {
    List<JsonNode> messages = new ArrayList<>();
    for (JsonNode message; messages.size() < limit && (message = inbox.poll()) != null; ) {
      messages.add(message);
    }
    return messages;
  }

  /**
   * Publishes the public state. Only the newest state is sent when several arrive while a request
   * is running.
   *
   * @param json state JSON
   */
  void publish(String json) {
    pendingState.set(json);
    synchronized (this) {
      if (publishing || session.get() == null) return;
      publishing = true;
    }
    Thread.ofVirtual().name("audience-relay-state").start(this::sendStates);
  }

  /**
   * Ends the session at the relay; devices show that it is over. It waits at most two seconds,
   * because the game usually exits right afterwards.
   */
  void close() {
    closed = true;
    Session current = session.get();
    if (current == null) return;
    send(request(current, "").timeout(Duration.ofSeconds(2)).DELETE().build());
  }

  private void sendStates() {
    while (true) {
      String json = pendingState.getAndSet(null);
      Session current = session.get();
      if (json == null || current == null || closed) {
        synchronized (this) {
          publishing = false;
          // A state may have arrived after the getAndSet; publish() saw publishing == true.
          if (pendingState.get() == null || closed) return;
          publishing = true;
        }
        continue;
      }
      HttpRequest put =
          request(current, "/state").PUT(HttpRequest.BodyPublishers.ofString(json)).build();
      if (send(put).isEmpty()) {
        pendingState.compareAndSet(null, json);
        sleep(RETRY);
      }
    }
  }

  private void run() {
    while (!closed && session.get() == null) {
      HttpRequest.Builder create =
          HttpRequest.newBuilder(uri("/api/game/sessions"))
              .timeout(Duration.ofSeconds(10))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(surveys));
      key.ifPresent(value -> create.header("Authorization", "Bearer " + value));
      Optional<JsonNode> created = send(create.build()).map(TrackingJson::object);
      if (created.isPresent()) {
        session.set(
            new Session(
                created.get().get("code").asString(), created.get().get("gameToken").asString()));
        LOGGER.info("Audience session {} created at {}", session.get().code(), relay);
        publish(Optional.ofNullable(pendingState.get()).orElse("{\"phase\":\"lobby\"}"));
      } else {
        sleep(RETRY);
      }
    }
    long after = 0;
    while (!closed) {
      HttpRequest poll =
          request(session.get(), "/inbox?after=" + after)
              .timeout(Duration.ofSeconds(40))
              .GET()
              .build();
      Optional<JsonNode> body = send(poll).map(TrackingJson::object);
      if (body.isEmpty()) {
        sleep(RETRY);
        continue;
      }
      for (JsonNode message : body.get().path("messages")) {
        long sequence = message.path("seq").asLong();
        if (sequence <= after) continue;
        after = sequence;
        inbox.add(message);
      }
    }
  }

  private HttpRequest.Builder request(Session current, String path) {
    return HttpRequest.newBuilder(uri("/api/game/sessions/" + current.code() + path))
        .timeout(Duration.ofSeconds(10))
        .header("Authorization", "Bearer " + current.token())
        .header("Content-Type", "application/json");
  }

  private URI uri(String path) {
    String base = relay.toString();
    return URI.create((base.endsWith("/") ? base.substring(0, base.length() - 1) : base) + path);
  }

  // Sends a request, records whether the relay answered, and returns the body of a successful
  // response.
  private Optional<String> send(HttpRequest request) {
    try {
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      online = response.statusCode() / 100 == 2;
      if (online) return Optional.of(response.body());
      LOGGER.warn("Relay answered {} to {}", response.statusCode(), request.uri().getPath());
      if (response.statusCode() == 401 && session.get() == null) {
        LOGGER.warn("The relay requires a key; set DUNGEON_AUDIENCE_RELAY_KEY when starting");
      }
      return Optional.empty();
    } catch (java.io.IOException exception) {
      online = false;
      LOGGER.warn("Relay unreachable: {}", exception.getMessage());
      return Optional.empty();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      closed = true;
      return Optional.empty();
    }
  }

  private static void sleep(Duration duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
    }
  }

  private record Session(String code, String token) {}
}
