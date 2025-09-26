package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

public record EntityDespawnEvent(int entityId, String reason) implements NetworkMessage {}
