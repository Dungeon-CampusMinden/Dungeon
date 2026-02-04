package core.network.server;

import core.Entity;
import core.Game;
import core.network.config.NetworkConfig;
import core.network.messages.c2s.InputMessage;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.LevelState;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side state tracking for a connected client. Manages sequence numbering, tick correlation,
 * RTT estimates, and reconnection logic to ensure robust input processing, lag compensation, and
 * session continuity. This class is mutable and thread-safe for read-mostly access (e.g., during
 * ticks), but write operations (e.g., seq updates) should be synchronized externally.
 *
 * @see AuthoritativeServerLoop
 * @see InputMessage
 */
public class ClientState {

  /** Unique client identifier assigned during handshake. */
  private final short clientId;

  /** Unique client username assigned during handshake. */
  private final String username;

  /** Session identifier for the current connection (changes on reconnect with new session). */
  private int sessionId;

  /**
   * Session token for validation during reconnect (random bytes). Used to confirm identity in
   * ReconnectRequest.
   */
  private byte[] sessionToken;

  /**
   * The last processed input sequence number. Used to detect and discard duplicates or stale inputs
   * (seq <= this value). Initialized to -1 (none processed).
   */
  private int lastProcessedSeq = -1;

  /**
   * The next expected input sequence number. Tracks gaps (for interpolation/warnings) when inputs
   * arrive out-of-order. Initialized to 0.
   */
  private int expectedSeq = 0;

  /**
   * The estimated round-trip time (RTT) in milliseconds. Updated based on clientTick vs. server
   * reception time; used for lag compensation. Initialized to 0 (unknown).
   */
  private float rttEstimateMs = 0.0f;

  /**
   * The last known client tick (from InputMessage.clientTick). Helps in correlating client time
   * with server ticks for prediction/reconciliation.
   */
  private long lastClientTick = 0;

  /**
   * The entity of the player (local player).
   *
   * <p>Null if not yet assigned (e.g., before spawn). May be preserved across reconnects if within
   * the reconnect window.
   */
  private Entity playerEntity = null;

  /**
   * Timestamp of the last activity (heartbeat, input, or event) in system millis. Used for
   * disconnect detection (timeout) and reconnect window eligibility.
   */
  private long lastActivityTimeMs;

  // ---- Delta Snapshot Caching ----

  /**
   * Cache of the last EntityState sent to this client, keyed by entity ID. Used for entity-level
   * delta comparison.
   */
  private final Map<Integer, EntityState> lastSentEntityStates = new ConcurrentHashMap<>();

  /**
   * Set of entity IDs that were visible to this client in the last snapshot. Used to detect
   * entities that have left the client's view and should be despawned.
   */
  private final Set<Integer> lastVisibleEntityIds = ConcurrentHashMap.newKeySet();

  /** The last LevelState sent to this client. Used for level delta comparison. */
  private volatile LevelState lastSentLevelState;

  /** The server tick when the last full snapshot was sent to this client. */
  private volatile int lastFullSnapshotTick = -1;

  /**
   * Set of static entity IDs that have been sent to this client in a full snapshot. Static entities
   * (no VelocityComponent or maxSpeed=0) are only sent once and don't need delta updates since they
   * never move. They persist until level change.
   */
  private final Set<Integer> sentStaticEntityIds = ConcurrentHashMap.newKeySet();

  /**
   * Constructs a new ClientState for a fresh connection.
   *
   * @param clientId The assigned client ID (unique per session).
   * @param username The client's username (non-null, non-empty).
   * @param sessionId The session ID
   * @param sessionToken A random token for reconnect validation.
   * @throws IllegalArgumentException If clientId < 0 or username is invalid.
   */
  public ClientState(short clientId, String username, int sessionId, byte[] sessionToken) {
    if (clientId < 0) {
      throw new IllegalArgumentException("clientId must be non-negative");
    }
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username must be non-null, non-empty");
    }
    if (sessionToken == null) {
      throw new IllegalArgumentException("sessionToken must be non-null");
    }

    this.clientId = clientId;
    this.username = username;
    this.sessionId = sessionId;
    this.sessionToken = sessionToken.clone();
    this.lastActivityTimeMs = System.currentTimeMillis();
  }

  /**
   * Resets the state for a reconnect scenario. Clears processed sequences and ticks but preserves
   * heroEntityId if in reconnect window. Updates sessionId and token for the new session.
   *
   * @param newSessionId The new session ID.
   * @param newSessionToken The new session token.
   * @param preserveHero If true, keeps the existing heroEntityId.
   * @throws IllegalArgumentException If newSessionId is 0 or token is invalid.
   * @throws IllegalStateException If preserveHero is true but no heroEntityId is assigned
   */
  public synchronized void resetForReconnect(
      int newSessionId, byte[] newSessionToken, boolean preserveHero) {
    if (newSessionId == 0L) {
      throw new IllegalArgumentException("newSessionId must be non-zero");
    }
    if (newSessionToken == null || newSessionToken.length == 0) {
      throw new IllegalArgumentException("newSessionToken must be non-null and non-empty");
    }

    this.sessionId = newSessionId;
    this.sessionToken = newSessionToken.clone();
    this.lastProcessedSeq = -1;
    this.expectedSeq = 0;
    this.lastClientTick = 0;
    if (!preserveHero) {
      Game.remove(this.playerEntity);
      this.playerEntity = null;
    }
    {
      if (this.playerEntity == null) {
        throw new IllegalStateException("Cannot preserve hero entity on reconnect; none assigned");
      }
      Game.add(this.playerEntity);
    }
    this.lastActivityTimeMs = System.currentTimeMillis();
  }

  /**
   * Updates the last processed sequence after successfully applying an input. Ensures monotonic
   * progress; throws if seq is not the expected next.
   *
   * @param seq The sequence number that was just processed (must be > lastProcessedSeq).
   * @throws IllegalArgumentException If seq <= lastProcessedSeq (stale or duplicate).
   */
  public synchronized void updateProcessedSeq(int seq) {
    if (seq <= this.lastProcessedSeq) {
      throw new IllegalArgumentException(
          "Cannot update to stale or duplicate seq: " + seq + " <= " + lastProcessedSeq);
    }
    this.lastProcessedSeq = seq;
    this.expectedSeq = seq + 1;
    this.lastActivityTimeMs = System.currentTimeMillis();
  }

  /**
   * Handles a sequence gap by updating expectedSeq and logging the gap for interpolation. Used when
   * an input arrives with seq > expectedSeq but within maxSeqGap.
   *
   * @param newSeq The arriving sequence number (must be > expectedSeq).
   * @return The gap size for interpolation logic (newSeq - expectedSeq).
   * @throws IllegalArgumentException If newSeq <= expectedSeq or gap > maxSeqGap.
   */
  public synchronized int handleSeqGap(int newSeq) {
    if (newSeq <= this.expectedSeq) {
      throw new IllegalArgumentException(
          "Invalid seq for gap handling: " + newSeq + " <= " + expectedSeq);
    }
    int gap = newSeq - this.expectedSeq;
    if (gap > NetworkConfig.MAX_SEQUENCE_GAP) {
      throw new IllegalArgumentException(
          "Seq gap too large: " + gap + " > " + NetworkConfig.MAX_SEQUENCE_GAP);
    }
    this.expectedSeq = newSeq;
    this.lastActivityTimeMs = System.currentTimeMillis();
    return gap;
  }

  /**
   * Updates the RTT estimate based on the time difference between clientTick and reception. Uses a
   * simple EWMA (Exponential Weighted Moving Average) for smoothing.
   *
   * @param clientTick The client's tick timestamp (in ms or tick units).
   * @param alpha The smoothing factor (0.0-1.0; e.g., 0.1 for gradual updates).
   */
  public synchronized void updateRttEstimate(long clientTick, float alpha) {
    long receptionTime = System.currentTimeMillis();
    long rawRtt = receptionTime - clientTick;
    if (rawRtt < 0) rawRtt = 0;

    this.rttEstimateMs = alpha * rawRtt + (1.0f - alpha) * this.rttEstimateMs;
    this.lastClientTick = clientTick;
    this.lastActivityTimeMs = receptionTime;
  }

  /**
   * Assigns or updates the hero entity (e.g., after spawn or restore).
   *
   * @param entity The entity representing the client's hero.
   * @throws IllegalArgumentException If entity is null.
   */
  public synchronized void playerEntity(Entity entity) {
    this.playerEntity = entity;
    this.lastActivityTimeMs = System.currentTimeMillis();
  }

  /** Marks the last activity time (e.g., for heartbeat updates). Used for timeout detection. */
  public synchronized void updateLastActivity() {
    this.lastActivityTimeMs = System.currentTimeMillis();
  }

  /**
   * Returns the assigned client ID.
   *
   * @return The client ID (never negative).
   */
  public short clientId() {
    return clientId;
  }

  /**
   * Returns the client's username.
   *
   * @return The username (never null/empty).
   */
  public String username() {
    return username;
  }

  /**
   * Returns the current session ID.
   *
   * @return The session ID (never 0).
   */
  public int sessionId() {
    return sessionId;
  }

  /**
   * Returns the last processed seq (initially -1).
   *
   * @return The last processed seq.
   */
  public int lastProcessedSeq() {
    return lastProcessedSeq;
  }

  /**
   * Returns the next expected input sequence number.
   *
   * @return The expected seq.
   */
  public int expectedSeq() {
    return expectedSeq;
  }

  /**
   * Returns the current RTT estimate in ms.
   *
   * @return The RTT (0 if unknown).
   */
  public float rttEstimateMs() {
    return rttEstimateMs;
  }

  /**
   * Returns the last known client tick.
   *
   * @return The client tick (0 if unknown).
   */
  public long lastClientTick() {
    return lastClientTick;
  }

  /**
   * Returns the optional hero entity. May be empty if not assigned.
   *
   * @return The optional hero entity.
   */
  public Optional<Entity> playerEntity() {
    return Optional.ofNullable(playerEntity);
  }

  /**
   * Returns an immutable copy of the session token.
   *
   * @return The token bytes (never null/empty).
   */
  public byte[] sessionToken() {
    return sessionToken.clone();
  }

  /**
   * Verifies a provided token against the stored session token.
   *
   * @param token token to verify
   * @return true if equal
   */
  public boolean verifyToken(byte[] token) {
    return token != null && Arrays.equals(sessionToken, token);
  }

  /**
   * Returns the last activity timestamp in system millis.
   *
   * @return The timestamp.
   */
  public long lastActivityTimeMs() {
    return lastActivityTimeMs;
  }

  /**
   * Checks if the client is eligible for reconnect (within window since last activity).
   *
   * @param currentTimeMs Current system time in ms.
   * @param reconnectWindowMs The window duration (e.g., 60000 for 60s).
   * @return True if within window.
   */
  public boolean isWithinReconnectWindow(long currentTimeMs, long reconnectWindowMs) {
    return (currentTimeMs - lastActivityTimeMs) <= reconnectWindowMs;
  }

  /**
   * Returns true if seq is plausibly recent (within gap tolerance from lastProcessedSeq). Accounts
   * for Integer overflow by using signed int arithmetic.
   *
   * @param seq The candidate sequence.
   * @return True if plausible (not stale).
   */
  public boolean isSeqPlausible(int seq) {
    if (lastProcessedSeq == -1) return true; // Fresh state

    int diff = seq - lastProcessedSeq;
    if (diff >= 0 && diff <= NetworkConfig.MAX_SEQUENCE_GAP) return true; // Forward, small gap
    if (diff < 0) {
      // Check for wrap-around (e.g., seq just wrapped, lastProcessedSeq near MAX_VALUE)
      int wrappedDiff = (seq + (Integer.MAX_VALUE - lastProcessedSeq) + 1);
      return wrappedDiff >= 0 && wrappedDiff <= NetworkConfig.MAX_SEQUENCE_GAP;
    }
    return false; // Too old or large gap
  }

  @Override
  public String toString() {
    return "ClientState(clientId='" + clientId + "', username='" + username + "')";
  }

  // region Delta Snapshot Caching

  /**
   * Returns the cache of last sent entity states.
   *
   * @return map of entity ID to last sent EntityState
   */
  public Map<Integer, EntityState> lastSentEntityStates() {
    return lastSentEntityStates;
  }

  /**
   * Returns the set of entity IDs that were visible to this client in the last snapshot.
   *
   * @return set of visible entity IDs
   */
  public Set<Integer> lastVisibleEntityIds() {
    return lastVisibleEntityIds;
  }

  /**
   * Returns the last LevelState sent to this client.
   *
   * @return the last sent LevelState, or null if none
   */
  public LevelState lastSentLevelState() {
    return lastSentLevelState;
  }

  /**
   * Sets the last LevelState sent to this client.
   *
   * @param levelState the LevelState to cache
   */
  public void lastSentLevelState(LevelState levelState) {
    this.lastSentLevelState = levelState;
  }

  /**
   * Returns the server tick when the last full snapshot was sent.
   *
   * @return the last full snapshot tick, or -1 if none
   */
  public int lastFullSnapshotTick() {
    return lastFullSnapshotTick;
  }

  /**
   * Sets the server tick when the last full snapshot was sent.
   *
   * @param tick the server tick
   */
  public void lastFullSnapshotTick(int tick) {
    this.lastFullSnapshotTick = tick;
  }

  /**
   * Returns the set of static entity IDs that have been sent to this client.
   *
   * @return set of static entity IDs
   */
  public Set<Integer> sentStaticEntityIds() {
    return sentStaticEntityIds;
  }

  /**
   * Clears all snapshot caching state. Call this on reconnection or level change to force a full
   * snapshot.
   */
  public void clearSnapshotCache() {
    lastSentEntityStates.clear();
    lastVisibleEntityIds.clear();
    sentStaticEntityIds.clear();
    lastSentLevelState = null;
    lastFullSnapshotTick = -1;
  }

  // endregion
}
