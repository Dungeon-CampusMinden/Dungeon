package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

/**
 * Server-to-client debug pong used for real RTT measurement.
 *
 * @param requestId echoed request identifier
 * @param clientTimeNanos echoed client monotonic timestamp
 * @param serverReceiveTimeMs server wall-clock receive time in milliseconds
 * @param serverSendTimeMs server wall-clock send time in milliseconds
 */
public record DebugPong(
    long requestId, long clientTimeNanos, long serverReceiveTimeMs, long serverSendTimeMs)
    implements NetworkMessage {}
