package core.network.messages.server2client;

import core.network.messages.NetworkMessage;
import core.utils.Direction;
import core.utils.Point;

public record PositionUpdate(int entityId, Point position, Direction viewDirection)
    implements NetworkMessage {}
