package core.network.messages.s2c;

import core.components.DrawComponent;
import core.components.PositionComponent;
import core.network.messages.NetworkMessage;

public record EntitySpawnEvent(
    int entityId,
    PositionComponent positionComponent,
    DrawComponent drawComponent,
    boolean isPersistent)
    implements NetworkMessage {}
