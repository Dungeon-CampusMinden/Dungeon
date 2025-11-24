package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

/**
 * Server→client: despawn entity with reason.
 *
 * <p>Expected max size: tiny (<= 16 bytes).
 *
 * @param entityId the entity's unique ID
 * @param reason the reason for despawning (e.g., "destroyed", "left game", etc.)
 */
public record EntityDespawnEvent(int entityId, String reason) implements NetworkMessage {}
