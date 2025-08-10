package core.network.messages;

/**
 * Client→server: UDP registration message sent after receiving ConnectAck.
 *
 * <p>This message is sent via UDP to establish the mapping between clientId and
 * the client's UDP address on the server side.
 *
 * @param clientId the client id assigned by server in ConnectAck
 */
public record RegisterUdp(int clientId) implements NetworkMessage {
}
