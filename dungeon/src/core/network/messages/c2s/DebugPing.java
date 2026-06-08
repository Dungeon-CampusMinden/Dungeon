package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/**
 * Client-to-server debug ping used for real RTT measurement.
 *
 * @param requestId client-generated request identifier
 * @param clientTimeNanos client monotonic timestamp from {@link java.lang.System#nanoTime()}
 */
public record DebugPing(long requestId, long clientTimeNanos) implements NetworkMessage {}
