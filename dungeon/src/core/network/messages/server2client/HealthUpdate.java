package core.network.messages.server2client;

import core.network.messages.NetworkMessage;

public record HealthUpdate(int entityId, int currentHealth, int maxHealth)
    implements NetworkMessage {}
