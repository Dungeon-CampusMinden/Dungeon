package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

/**
 * Server→client: reject connection attempt with reason.
 *
 * <p>Expected max size: tiny (<= 16 bytes).
 */
public record ConnectReject(String reason) implements NetworkMessage {}
