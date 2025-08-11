package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

/**
 * Server→client: acknowledgement of connection with assigned clientId.
 *
 * <p>Temporary Java-serialized prototype to be replaced by protobuf later. Expected max size: tiny
 * (<= 16 bytes).
 */
public record ConnectAck(int clientId) implements NetworkMessage {
}
