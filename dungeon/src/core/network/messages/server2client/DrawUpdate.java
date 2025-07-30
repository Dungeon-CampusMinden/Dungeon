package core.network.messages.server2client;

import core.network.messages.NetworkMessage;

public record DrawUpdate(int entityId, String animationName, int tintColor)
    implements NetworkMessage {}
