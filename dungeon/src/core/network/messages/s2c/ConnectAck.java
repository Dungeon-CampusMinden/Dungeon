package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

/**
 * Server→client: acknowledgement of connection with assigned clientId.
 *
 * <p>Expected max size: tiny (<= 16 bytes).
 */
public record ConnectAck(int clientId) implements NetworkMessage {}
