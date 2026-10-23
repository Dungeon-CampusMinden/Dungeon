package audience.relay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * One game on a shared screen and the devices that joined it.
 *
 * <p>The relay keeps no game logic. The game publishes its public state, which every device
 * streams, and reads the devices' inputs from an inbox in arrival order. Devices authenticate with
 * a secret token; the game only sees their public participant ID ({@code pid}).
 */
final class RelaySession {
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int MAX_INBOX = 20_000;
  private static final long MIN_INPUT_GAP_NANOS = 100_000_000L;

  private final String code;
  private final String gameToken;
  private final JsonNode surveys;
  private final int maxParticipants;
  private final Map<String, String> pidByToken = new HashMap<>();
  private final Map<String, Long> lastInputByPid = new HashMap<>();
  private final Map<String, Long> streamByPid = new HashMap<>();
  private final ArrayDeque<ObjectNode> inbox = new ArrayDeque<>();

  private String state = "{\"phase\":\"lobby\"}";
  private long stateVersion = 1;
  private long stateAt = System.nanoTime();
  private long nextSequence = 1;
  private long nextStream = 1;
  private int nextPid = 1;
  private Instant lastGameContact = Instant.now();
  private boolean closed;

  RelaySession(String code, String gameToken, JsonNode surveys, int maxParticipants) {
    this.code = code;
    this.gameToken = gameToken;
    this.surveys = surveys;
    this.maxParticipants = maxParticipants;
  }

  /**
   * @return a new random URL-safe secret
   */
  static String newToken() {
    byte[] bytes = new byte[24];
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  String code() {
    return code;
  }

  boolean gameAuthorized(String token) {
    return matches(gameToken, token);
  }

  /**
   * Compares a secret in constant time.
   *
   * @param secret expected secret
   * @param candidate value a client sent, may be null
   * @return whether both are equal
   */
  static boolean matches(String secret, String candidate) {
    return candidate != null
        && MessageDigest.isEqual(
            secret.getBytes(StandardCharsets.UTF_8), candidate.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * @return the survey definitions the game registered, keyed by phase
   */
  JsonNode surveys() {
    return surveys;
  }

  /**
   * Joins a device, or rejoins it when it presents a token of this session again.
   *
   * @param token token stored by the device, if any
   * @return the device's token and participant ID
   * @throws IllegalStateException if the session is closed or full
   */
  synchronized Joined join(Optional<String> token) {
    if (closed) throw new IllegalStateException("Session is closed");
    if (token.isPresent() && pidByToken.containsKey(token.get())) {
      return new Joined(token.get(), pidByToken.get(token.get()));
    }
    if (pidByToken.size() >= maxParticipants) throw new IllegalStateException("Session is full");
    String newToken = newToken();
    String pid = "p" + nextPid++;
    pidByToken.put(newToken, pid);
    ObjectNode message = JsonMapper.shared().createObjectNode().put("type", "join");
    enqueue(pid, message);
    return new Joined(newToken, pid);
  }

  /**
   * @param token device token
   * @return the participant ID of a joined device
   */
  synchronized Optional<String> pid(String token) {
    return Optional.ofNullable(token == null ? null : pidByToken.get(token));
  }

  /**
   * Registers a new event stream of a device, which replaces its previous one: a device holds at
   * most one stream, and a reloaded page or a device that switched networks never waits for a dead
   * connection to time out.
   *
   * @param pid participant ID
   * @return ID of the stream for {@link #awaitState}
   */
  synchronized long openStream(String pid) {
    long stream = nextStream++;
    streamByPid.put(pid, stream);
    notifyAll();
    return stream;
  }

  private boolean replaced(String pid, long stream) {
    return streamByPid.getOrDefault(pid, stream) != stream;
  }

  /**
   * Queues a device's input for the game.
   *
   * @param pid participant ID of the sender
   * @param message input without the token
   * @return whether the input was queued; a device sending faster than ten inputs per second is
   *     refused
   * @throws IllegalStateException if the session is closed or the game stopped reading
   */
  synchronized boolean input(String pid, ObjectNode message) {
    if (closed) throw new IllegalStateException("Session is closed");
    if (inbox.size() >= MAX_INBOX) throw new IllegalStateException("Game is not reading");
    long now = System.nanoTime();
    // Only accepted inputs count, so a burst of refused ones does not lock the device out.
    Long last = lastInputByPid.get(pid);
    if (last != null && now - last < MIN_INPUT_GAP_NANOS) return false;
    lastInputByPid.put(pid, now);
    enqueue(pid, message);
    return true;
  }

  private void enqueue(String pid, ObjectNode message) {
    message.put("seq", nextSequence++).put("pid", pid);
    inbox.addLast(message);
    notifyAll();
  }

  /**
   * Waits for inputs newer than {@code after} and drops the older ones, which the game confirmed by
   * asking for later ones.
   *
   * @param after highest sequence the game has processed
   * @param timeout longest wait
   * @return inputs in arrival order, empty after the timeout
   */
  synchronized List<ObjectNode> awaitInbox(long after, Duration timeout)
      throws InterruptedException {
    lastGameContact = Instant.now();
    while (!inbox.isEmpty() && inbox.peekFirst().get("seq").asLong() <= after) {
      inbox.removeFirst();
    }
    long remaining = timeout.toNanos();
    long deadline = System.nanoTime() + remaining;
    while (inbox.isEmpty() && !closed && remaining > 0) {
      TimeUnit.NANOSECONDS.timedWait(this, remaining);
      remaining = deadline - System.nanoTime();
    }
    lastGameContact = Instant.now();
    return new ArrayList<>(inbox);
  }

  /**
   * Replaces the public state that devices display.
   *
   * @param json state as a JSON object
   */
  synchronized void state(String json) {
    lastGameContact = Instant.now();
    state = json;
    stateVersion++;
    stateAt = System.nanoTime();
    notifyAll();
  }

  /**
   * Waits until the state differs from the version a device has seen.
   *
   * @param seenVersion state version the device already received, {@code 0} for none
   * @param timeout longest wait
   * @param pid participant ID of the device
   * @param stream ID of the waiting stream from {@link #openStream}
   * @return the current state, or empty after the timeout without a change
   * @throws StreamReplacedException if the device opened a newer stream
   */
  synchronized Optional<State> awaitState(
      long seenVersion, Duration timeout, String pid, long stream) throws InterruptedException {
    long remaining = timeout.toNanos();
    long deadline = System.nanoTime() + remaining;
    while (stateVersion == seenVersion && remaining > 0 && !replaced(pid, stream)) {
      TimeUnit.NANOSECONDS.timedWait(this, remaining);
      remaining = deadline - System.nanoTime();
    }
    if (replaced(pid, stream)) throw new StreamReplacedException();
    long ageMs = (System.nanoTime() - stateAt) / 1_000_000;
    return stateVersion == seenVersion
        ? Optional.empty()
        : Optional.of(new State(stateVersion, state, ageMs, closed));
  }

  /** Ends the session: devices receive a closed state and the game's long poll returns. */
  synchronized void close() {
    closed = true;
    state = "{\"phase\":\"closed\"}";
    stateVersion++;
    stateAt = System.nanoTime();
    notifyAll();
  }

  /**
   * @param now current time
   * @param idleTimeout allowed time without game contact
   * @return whether the session can be removed
   */
  synchronized boolean expired(Instant now, Duration idleTimeout) {
    return lastGameContact.plus(idleTimeout).isBefore(now);
  }

  /** Ends an event stream whose device opened a newer one. */
  static final class StreamReplacedException extends RuntimeException {
    StreamReplacedException() {
      super("Replaced by a newer stream", null, false, false);
    }
  }

  /**
   * A device's credentials.
   *
   * @param token secret the device sends with every request
   * @param pid public participant ID the game sees
   */
  record Joined(String token, String pid) {}

  /**
   * A published state.
   *
   * @param version increasing version
   * @param json state JSON
   * @param ageMs milliseconds since the game published it; timers in the state count from then
   * @param closed whether the session ended
   */
  record State(long version, String json, long ageMs, boolean closed) {}
}
