package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/**
 * Client→server: UDP registration message sent after receiving ConnectAck.
 *
 * <p>This message is sent via UDP to establish the mapping between clientId and the client's UDP
 * address on the server side.
 *
 * @param clientId the client id assigned by server in ConnectAck
 */
public record RegisterUdp(short clientId) implements NetworkMessage {}
