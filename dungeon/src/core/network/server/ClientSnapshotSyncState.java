package core.network.server;

import core.network.FullSnapshotSendReason;

/** Tracks per-client snapshot acknowledgement and full-snapshot pacing state. */
public final class ClientSnapshotSyncState {
  private int lastAckedSnapshotTick = -1;
  private int lastFullSnapshotTick = -1;
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
   * Marks a full snapshot as sent.
   *
   * @param serverTick full snapshot tick
   */
  public synchronized void markFullSnapshotSent(int serverTick) {
    lastFullSnapshotTick = serverTick;
  }

  /**
   * Returns whether a full snapshot should be sent now.
   *
   * @param currentTick current server tick
   * @param intervalTicks full-snapshot interval in ticks
   * @return true when no full snapshot was sent or the interval elapsed
   */
  public synchronized boolean fullSnapshotDue(int currentTick, int intervalTicks) {
    return lastFullSnapshotTick < 0 || currentTick - lastFullSnapshotTick >= intervalTicks;
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
    pendingFullSnapshotReason =
        reason == null ? FullSnapshotSendReason.SERVER_FORCED_RESYNC : reason;
  }
}
