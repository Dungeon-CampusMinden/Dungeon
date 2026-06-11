package core.network.server;

/** Tracks per-client snapshot acknowledgement and full-snapshot pacing state. */
public final class ClientSnapshotSyncState {
  private int lastAckedSnapshotTick = -1;
  private int lastFullSnapshotTick = -1;

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

  /** Resets all snapshot sync state. */
  public synchronized void clear() {
    lastAckedSnapshotTick = -1;
    lastFullSnapshotTick = -1;
  }
}
