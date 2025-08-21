package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/**
 * A message from a client to the server to request the spawn of an entity. This is used when the
 * client receives a snapshot for an entity it does not know yet.
 *
 * @param entityName The name of the entity to spawn.
 */
public record RequestEntitySpawn(int entityId) implements NetworkMessage {}
