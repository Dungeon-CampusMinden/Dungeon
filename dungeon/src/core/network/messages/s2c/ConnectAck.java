package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

/**
 * Server→client: acknowledgement of connection with assigned clientId.
 *
 * <p>Expected max size: tiny (<= 16 bytes).
 *
 * @param clientId the assigned client ID
 * @param sessionId the assigned session ID
 * @param sessionToken the assigned session token
 */
public record ConnectAck(short clientId, int sessionId, byte[] sessionToken)
    implements NetworkMessage {}
