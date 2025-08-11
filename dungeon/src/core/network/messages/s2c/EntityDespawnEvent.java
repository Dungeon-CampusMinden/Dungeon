package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

public record EntityDespawnEvent(String entityName, String reason) implements NetworkMessage {}
