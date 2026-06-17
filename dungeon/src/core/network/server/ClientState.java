package core.network.server;

import contrib.entities.CharacterClass;
import core.Entity;
import core.Game;
import core.network.FullSnapshotSendReason;
import core.network.config.NetworkConfig;
import core.network.delta.SnapshotHistory;
import core.network.messages.c2s.InputMessage;
import core.network.messages.s2c.SnapshotMessage;
import java.util.Arrays;
import java.util.Collection;
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

  /** Selected character class for this client. Preserved across reconnects. */
  private final CharacterClass characterClass;

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

  /** The estimated round-trip time (RTT) in milliseconds. Initialized to 0 (unknown). */
  private volatile float rttEstimateMs = 0.0f;

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
  private volatile long lastActivityTimeMs;

  /** True after the client confirms that its initial world bootstrap was applied. */
  private volatile boolean initialWorldReady = false;

  /** Per-client server-side snapshot acknowledgement state. */
  private final ClientSnapshotSyncState snapshotSync = new ClientSnapshotSyncState();

  /** Client-side history of fully applied snapshots. */
  private final SnapshotHistory appliedSnapshotHistory =
      new SnapshotHistory(NetworkConfig.CLIENT_DELTA_HISTORY_SIZE);

  /** Latest snapshot tick applied by this client. */
  private volatile int latestAppliedSnapshotTick = -1;

  /** Entity IDs known by the client through reliable lifecycle events or snapshots. */
  private final Set<Integer> networkSyncedEntityIds = ConcurrentHashMap.newKeySet();

  /** Entity IDs sent to this client for the current acknowledged delta baseline. */
  private final Set<Integer> knownSnapshotEntityIds = ConcurrentHashMap.newKeySet();

  /** Server tick of the baseline used by knownSnapshotEntityIds. */
  private volatile int knownSnapshotBaseTick = -1;

  /**
   * Constructs a new ClientState for a fresh connection.
   *
   * @param clientId The assigned client ID (unique per session).
   * @param username The client's username (non-null, non-empty).
   * @param sessionId The session ID
   * @param sessionToken A random token for reconnect validation.
   * @param characterClass The selected character class for this client.
   * @throws IllegalArgumentException If clientId < 0 or username is invalid.
   */
  public ClientState(
      short clientId,
      String username,
      int sessionId,
      byte[] sessionToken,
      CharacterClass characterClass) {
    if (clientId < 0) {
      throw new IllegalArgumentException("clientId must be non-negative");
    }
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username must be non-null, non-empty");
    }
    if (sessionToken == null) {
      throw new IllegalArgumentException("sessionToken must be non-null");
    }
    if (characterClass == null) {
      throw new IllegalArgumentException("characterClass must be non-null");
    }

    this.clientId = clientId;
    this.username = username;
    this.characterClass = characterClass;
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
    this.initialWorldReady = false;
    clearSnapshotBaseline(FullSnapshotSendReason.RECONNECT);
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
   * Advances the processed input sequence if the provided sequence is newer than the current one.
   *
   * <p>Stale or duplicate inputs are expected under real network conditions, so they are reported
   * via the return value instead of an exception.
   *
   * @param seq The sequence number that was just dequeued for processing.
   * @return true when the sequence was accepted, false when it was stale or duplicated.
   */
  public synchronized boolean advanceProcessedSeq(int seq) {
    if (seq <= this.lastProcessedSeq) {
      return false;
    }
    this.lastProcessedSeq = seq;
    this.expectedSeq = seq + 1;
    this.lastActivityTimeMs = System.currentTimeMillis();
    return true;
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
   * Records a client-measured debug RTT sample.
   *
   * <p>Input messages carry game ticks, not synchronized wall-clock timestamps, so authoritative
   * server telemetry uses debug ping samples for an actual round-trip estimate.
   *
   * @param rttMs client-measured round-trip time in milliseconds
   * @param alpha EWMA smoothing factor between 0 and 1
   */
  public synchronized void recordDebugRttEstimate(float rttMs, float alpha) {
    if (!Float.isFinite(rttMs) || rttMs <= 0f) {
      return;
    }
    float clampedAlpha = Math.max(0f, Math.min(1f, alpha));
    rttEstimateMs =
        rttEstimateMs > 0f ? clampedAlpha * rttMs + (1.0f - clampedAlpha) * rttEstimateMs : rttMs;
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
   * Returns whether this client has completed its initial world bootstrap.
   *
   * @return true once the client sent {@code InitialWorldReady}
   */
  public boolean initialWorldReady() {
    return initialWorldReady;
  }

  /**
   * Updates whether this client has completed its initial world bootstrap.
   *
   * @param initialWorldReady true once the client sent {@code InitialWorldReady}
   */
  public void initialWorldReady(boolean initialWorldReady) {
    this.initialWorldReady = initialWorldReady;
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
   * Returns the selected character class for this client.
   *
   * @return the selected character class
   */
  public CharacterClass characterClass() {
    return characterClass;
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

  /**
   * Returns per-client server-side snapshot sync state.
   *
   * @return snapshot sync state
   */
  public ClientSnapshotSyncState snapshotSync() {
    return snapshotSync;
  }

  /** Clears snapshot acknowledgement, applied-history, and delta tracking state. */
  public void clearSnapshotBaseline() {
    clearSnapshotBaseline(FullSnapshotSendReason.SERVER_FORCED_RESYNC);
  }

  /**
   * Clears snapshot acknowledgement, applied-history, and delta tracking state.
   *
   * @param reason reason why the next full snapshot should be sent
   */
  public void clearSnapshotBaseline(FullSnapshotSendReason reason) {
    this.latestAppliedSnapshotTick = -1;
    this.snapshotSync.clear(reason);
    this.appliedSnapshotHistory.clear();
    this.knownSnapshotEntityIds.clear();
    this.knownSnapshotBaseTick = -1;
  }

  /**
   * Requests a recovery full snapshot without clearing full-snapshot retry state.
   *
   * @param reason reason why this client needs a fresh full snapshot baseline
   */
  public void requestSnapshotResync(FullSnapshotSendReason reason) {
    this.snapshotSync.requestRecoveryFullSnapshot(reason);
    this.knownSnapshotEntityIds.clear();
    this.knownSnapshotBaseTick = -1;
  }

  /**
   * Returns the network-synchronized entity IDs currently tracked by the client.
   *
   * @return an immutable copy of tracked entity IDs
   */
  public Set<Integer> networkSyncedEntityIds() {
    return Set.copyOf(networkSyncedEntityIds);
  }

  /**
   * Tracks a local entity as network synchronized.
   *
   * @param entityId the entity ID
   */
  public void trackNetworkEntity(int entityId) {
    networkSyncedEntityIds.add(entityId);
  }

  /**
   * Stops tracking a local entity as network synchronized.
   *
   * @param entityId the entity ID
   */
  public void untrackNetworkEntity(int entityId) {
    networkSyncedEntityIds.remove(entityId);
  }

  /** Clears all locally tracked network-synchronized entity IDs. */
  public void clearNetworkEntities() {
    networkSyncedEntityIds.clear();
  }

  /**
   * Returns entity IDs known for the current acknowledged delta baseline.
   *
   * @return an immutable copy of known snapshot entity IDs
   */
  public Set<Integer> knownSnapshotEntityIds() {
    return Set.copyOf(knownSnapshotEntityIds);
  }

  /**
   * Resets the known snapshot entity IDs to the entities contained in a baseline snapshot.
   *
   * @param snapshot the new full snapshot baseline
   */
  public void resetKnownSnapshotEntityIds(SnapshotMessage snapshot) {
    knownSnapshotEntityIds.clear();
    snapshot.entities().forEach(entity -> knownSnapshotEntityIds.add(entity.entityId()));
    knownSnapshotBaseTick = snapshot.serverTick();
  }

  /**
   * Resets known entity tracking when the acknowledged delta baseline changes.
   *
   * @param baseline the acknowledged baseline snapshot
   */
  public void ensureKnownSnapshotEntityIdsForBaseline(SnapshotMessage baseline) {
    if (knownSnapshotBaseTick != baseline.serverTick()) {
      resetKnownSnapshotEntityIds(baseline);
    }
  }

  /**
   * Tracks entity IDs included in a delta snapshot for the current acknowledged baseline.
   *
   * @param entityIds entity IDs included in a delta snapshot
   */
  public void trackKnownSnapshotEntityIds(Collection<Integer> entityIds) {
    knownSnapshotEntityIds.addAll(entityIds);
  }

  /**
   * Returns the latest snapshot tick applied by this client.
   *
   * @return latest applied snapshot tick, or -1 if none has been applied
   */
  public int latestAppliedSnapshotTick() {
    return latestAppliedSnapshotTick;
  }

  /**
   * Updates the latest applied snapshot tick.
   *
   * @param serverTick the accepted server tick
   */
  public void latestAppliedSnapshotTick(int serverTick) {
    this.latestAppliedSnapshotTick = serverTick;
  }

  /**
   * Stores a fully applied client-side snapshot.
   *
   * @param snapshot applied full/materialized snapshot
   */
  public void rememberAppliedSnapshot(SnapshotMessage snapshot) {
    appliedSnapshotHistory.add(snapshot);
    latestAppliedSnapshotTick(snapshot.serverTick());
  }

  /**
   * Stores a fully applied client-side snapshot while retaining active delta baselines.
   *
   * @param snapshot applied full/materialized snapshot
   * @param protectedSnapshotTicks snapshot ticks that must remain available for delta
   *     materialization
   */
  public void rememberAppliedSnapshot(
      SnapshotMessage snapshot, Collection<Integer> protectedSnapshotTicks) {
    appliedSnapshotHistory.add(snapshot, protectedSnapshotTicks);
    latestAppliedSnapshotTick(snapshot.serverTick());
  }

  /**
   * Finds an applied client-side snapshot by server tick.
   *
   * @param serverTick server tick to look up
   * @return applied snapshot, if retained
   */
  public Optional<SnapshotMessage> appliedSnapshot(int serverTick) {
    return appliedSnapshotHistory.snapshot(serverTick);
  }

  /**
   * Returns the latest applied client-side snapshot.
   *
   * @return latest applied snapshot, if any
   */
  public Optional<SnapshotMessage> latestAppliedSnapshot() {
    return appliedSnapshotHistory.newest();
  }

  @Override
  public String toString() {
    return "ClientState(clientId='" + clientId + "', username='" + username + "')";
  }
}
