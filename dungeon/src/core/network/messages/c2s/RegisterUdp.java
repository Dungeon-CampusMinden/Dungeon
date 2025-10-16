package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/**
 * Client→server: UDP registration message sent after receiving ConnectAck.
 *
 * <p>This message is sent via UDP to establish the mapping between clientId and the client's UDP
 * address on the server side.
 *
 * @param sessionId the session ID of the client sending the message
 * @param sessionToken the session token of the client sending the message, must match the one
 *     assigned by the server in ConnectAck
 * @param clientId the client id assigned by server in ConnectAck
 */
public record RegisterUdp(int sessionId, byte[] sessionToken, short clientId)
    implements NetworkMessage {}
