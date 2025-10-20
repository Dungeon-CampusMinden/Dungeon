package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

/**
 * Server→client: reject connection attempt with reason.
 *
 * <p>Sent by the server when a client attempts to connect but is rejected for some reason (e.g.,
 * server full, invalid player name, incompatible version).
 *
 * @param reason The reason for the connection rejection.
 */
public record ConnectReject(String reason) implements NetworkMessage {}
