package core.network.server;

import core.network.FullSnapshotSendReason;
import java.util.HashSet;
import java.util.Set;

/** Tracks per-client snapshot acknowledgement and full-snapshot pacing state. */
public final class ClientSnapshotSyncState {
  private int lastAckedSnapshotTick = -1;
  private int lastFullSnapshotTick = -1;
  private int lastFullSnapshotAttemptTick = -1;
  private int lastDeltaSnapshotTick = -1;
  private FullSnapshotSendReason pendingFullSnapshotReason = FullSnapshotSendReason.NO_ACK;

  /**
   * Returns the highest snapshot tick acknowledged by the client.
   *
   * @return acknowledged snapshot tick, or -1 when no ack has arrived
   */
  public synchronized int lastAckedSnapshotTick() {
    return lastAckedSnapshotTick;
  }

  /**
   * Returns the last full snapshot tick sent to the client.
   *
   * @return full snapshot tick, or -1 when none was sent
   */
  public synchronized int lastFullSnapshotTick() {
    return lastFullSnapshotTick;
  }

  /**
   * Returns the last full snapshot tick scheduled for this client.
   *
   * @return full snapshot attempt tick, or -1 when none was scheduled
   */
  public synchronized int lastFullSnapshotAttemptTick() {
    return lastFullSnapshotAttemptTick;
  }

  /**
   * Returns the last delta snapshot tick sent to this client.
   *
   * @return delta snapshot tick, or -1 when none was sent
   */
  public synchronized int lastDeltaSnapshotTick() {
    return lastDeltaSnapshotTick;
  }

  /**
   * Records an acknowledgement if it moves the client forward.
   *
   * @param serverTick acknowledged snapshot tick
   */
  public synchronized void acknowledge(int serverTick) {
    if (serverTick > lastAckedSnapshotTick) {
      lastAckedSnapshotTick = serverTick;
    }
  }

  /**
   * Returns whether the client has acknowledged any snapshot.
   *
   * @return true when at least one ack has arrived
   */
  public synchronized boolean hasAck() {
    return lastAckedSnapshotTick >= 0;
  }

  /**
   * Marks a full snapshot as scheduled for sending.
   *
   * @param serverTick full snapshot tick
   */
  public synchronized void markFullSnapshotScheduled(int serverTick) {
    if (serverTick > lastFullSnapshotAttemptTick) {
      lastFullSnapshotAttemptTick = serverTick;
    }
  }

  /**
   * Marks a full snapshot as successfully sent.
   *
   * @param serverTick full snapshot tick
   */
  public synchronized void markFullSnapshotSent(int serverTick) {
    if (serverTick > lastFullSnapshotTick) {
      lastFullSnapshotTick = serverTick;
    }
    if (serverTick > lastFullSnapshotAttemptTick) {
      lastFullSnapshotAttemptTick = serverTick;
    }
  }

  /**
   * Clears an unsuccessful full-snapshot attempt so the next snapshot pass can retry.
   *
   * @param serverTick failed full snapshot tick
   */
  public synchronized void markFullSnapshotSendFailed(int serverTick) {
    if (serverTick == lastFullSnapshotAttemptTick && serverTick > lastFullSnapshotTick) {
      lastFullSnapshotAttemptTick = lastFullSnapshotTick;
    }
  }

  /**
   * Marks a delta snapshot as sent to this client.
   *
   * @param serverTick delta snapshot tick
   */
  public synchronized void markDeltaSnapshotSent(int serverTick) {
    if (serverTick > lastDeltaSnapshotTick) {
      lastDeltaSnapshotTick = serverTick;
    }
  }

  /**
   * Returns whether a full snapshot should be sent now.
   *
   * @param currentTick current server tick
   * @param intervalTicks full-snapshot interval in ticks
   * @return true when no full snapshot was sent or the interval elapsed
   */
  public synchronized boolean fullSnapshotDue(int currentTick, int intervalTicks) {
    int lastFullBaselineTick = Math.max(lastFullSnapshotTick, lastFullSnapshotAttemptTick);
    return lastFullBaselineTick < 0 || currentTick - lastFullBaselineTick >= intervalTicks;
  }

  /**
   * Returns whether recovery full snapshots may be retried.
   *
   * <p>A full snapshot newer than the latest acknowledgement is treated as in flight. This prevents
   * missing-baseline and no-ack recovery from sending one reliable full snapshot every snapshot
   * tick while the client is still receiving or acknowledging the first one.
   *
   * @param currentTick current server tick
   * @param retryIntervalTicks minimum retry interval in ticks
   * @return true when no newer full snapshot is in flight or the retry interval elapsed
   */
  public synchronized boolean fullSnapshotRecoveryDue(int currentTick, int retryIntervalTicks) {
    int lastFullBaselineTick = Math.max(lastFullSnapshotTick, lastFullSnapshotAttemptTick);
    if (lastFullBaselineTick < 0 || lastAckedSnapshotTick >= lastFullBaselineTick) {
      return true;
    }
    return currentTick - lastFullBaselineTick >= retryIntervalTicks;
  }

  /**
   * Records the reason that should be attached to the next full snapshot sent to this client.
   *
   * @param reason full snapshot reason
   */
  public synchronized void pendingFullSnapshotReason(FullSnapshotSendReason reason) {
    pendingFullSnapshotReason =
        reason == null ? FullSnapshotSendReason.SERVER_FORCED_RESYNC : reason;
  }

  /**
   * Returns the currently pending full-snapshot reason.
   *
   * @return pending full snapshot reason
   */
  public synchronized FullSnapshotSendReason pendingFullSnapshotReason() {
    return pendingFullSnapshotReason;
  }

  /**
   * Returns snapshot ticks that should remain retained for this client.
   *
   * @return protected snapshot ticks for acknowledged and in-flight baselines
   */
  public synchronized Set<Integer> protectedSnapshotTicks() {
    Set<Integer> ticks = new HashSet<>();
    if (lastAckedSnapshotTick >= 0) {
      ticks.add(lastAckedSnapshotTick);
    }
    if (lastFullSnapshotAttemptTick >= 0 && lastAckedSnapshotTick < lastFullSnapshotAttemptTick) {
      ticks.add(lastFullSnapshotAttemptTick);
    }
    if (lastDeltaSnapshotTick >= 0 && lastAckedSnapshotTick < lastDeltaSnapshotTick) {
      ticks.add(lastDeltaSnapshotTick);
    }
    return Set.copyOf(ticks);
  }

  /** Resets all snapshot sync state. */
  public synchronized void clear() {
    clear(FullSnapshotSendReason.SERVER_FORCED_RESYNC);
  }

  /**
   * Resets all snapshot sync state and records why a fresh full snapshot is needed.
   *
   * @param reason reason for invalidating the current baseline
   */
  public synchronized void clear(FullSnapshotSendReason reason) {
    lastAckedSnapshotTick = -1;
    lastFullSnapshotTick = -1;
    lastFullSnapshotAttemptTick = -1;
    lastDeltaSnapshotTick = -1;
    pendingFullSnapshotReason =
        reason == null ? FullSnapshotSendReason.SERVER_FORCED_RESYNC : reason;
  }
}
