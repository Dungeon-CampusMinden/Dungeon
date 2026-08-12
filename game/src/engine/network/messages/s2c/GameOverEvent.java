package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;

/**
 * Server→client: notify that the game is over.
 *
 * <p>Expected max size: tiny (<= 16 bytes).
 *
 * @param reason the reason for game over (e.g., "all_levels_completed", etc.)
 */
public record GameOverEvent(String reason) implements NetworkMessage {}
