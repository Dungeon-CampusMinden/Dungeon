package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/**
 * Client-to-server acknowledgement for the highest successfully applied snapshot tick.
 *
 * @param serverTick acknowledged server snapshot tick
 */
public record SnapshotAck(int serverTick) implements NetworkMessage {}
