package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/**
 * Client-to-server acknowledgement for the highest successfully applied snapshot tick.
 *
 * <p>The client can also use this reliable control message to request a full-snapshot recovery when
 * a received delta references a local baseline that is no longer retained.
 *
 * @param serverTick acknowledged server snapshot tick, or -1 when no snapshot is applied yet
 * @param resyncRequested true when the client is requesting a recovery full snapshot
 * @param missingBaseTick delta baseline tick missing on the client, or -1 when not requesting
 *     resync
 * @param deltaTick delta snapshot tick that exposed the missing baseline, or -1 when not requesting
 *     resync
 */
public record SnapshotAck(
    int serverTick, boolean resyncRequested, int missingBaseTick, int deltaTick)
    implements NetworkMessage {

  /**
   * Creates a plain snapshot acknowledgement.
   *
   * @param serverTick acknowledged server snapshot tick
   */
  public SnapshotAck(int serverTick) {
    this(serverTick, false, -1, -1);
  }

  /**
   * Creates a reliable resync request for a missing local delta baseline.
   *
   * @param latestAppliedTick highest snapshot tick still applied locally, or -1
   * @param missingBaseTick delta baseline tick missing on the client
   * @param deltaTick delta snapshot tick that exposed the missing baseline
   * @return snapshot acknowledgement carrying a resync request
   */
  public static SnapshotAck requestResync(
      int latestAppliedTick, int missingBaseTick, int deltaTick) {
    return new SnapshotAck(latestAppliedTick, true, missingBaseTick, deltaTick);
  }

  /** Normalizes unused resync detail fields on plain acknowledgements. */
  public SnapshotAck {
    if (!resyncRequested) {
      missingBaseTick = -1;
      deltaTick = -1;
    }
  }
}
