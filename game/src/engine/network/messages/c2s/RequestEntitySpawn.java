package engine.network.messages.c2s;

import engine.network.messages.NetworkMessage;

/**
 * A message from a client to the server to request the spawn of an entity. This is used when the
 * client receives a snapshot for an entity it does not know yet.
 *
 * @param entityId The id of the entity to spawn.
 */
public record RequestEntitySpawn(int entityId) implements NetworkMessage {}
