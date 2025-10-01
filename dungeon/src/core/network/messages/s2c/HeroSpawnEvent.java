package core.network.messages.s2c;

import core.network.messages.NetworkMessage;

public record HeroSpawnEvent(int entityId) implements NetworkMessage {}
