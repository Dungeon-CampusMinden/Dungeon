package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;

/**
 * Server→client: acknowledge udp registration.
 *
 * <p>Expected max size: tiny (<= 16 bytes).
 *
 * @param ok true if registration was successful, false otherwise
 */
public record RegisterAck(boolean ok) implements NetworkMessage {}
