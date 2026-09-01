package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;

/**
 * Server→client: acknowledgement of connection with assigned clientId.
 *
 * <p>Expected max size: tiny (<= 16 bytes).
 *
 * @param clientId the assigned client ID
 * @param sessionId the assigned session ID
 * @param sessionToken the assigned session token
 * @param trackingRoomId stable room ID for client-local history, or blank when tracking is disabled
 */
public record ConnectAck(short clientId, int sessionId, byte[] sessionToken, String trackingRoomId)
    implements NetworkMessage {}
