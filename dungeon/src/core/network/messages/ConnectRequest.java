package core.network.messages;

/**
 * Client→server: initial handshake request.
 *
 * <p>Temporary Java-serialized prototype to be replaced by protobuf later. Expected max size: tiny
 * (a few dozen bytes).
 *
 * @param playerName optional, may be empty
 */
public record ConnectRequest(String playerName) implements NetworkMessage {
}
