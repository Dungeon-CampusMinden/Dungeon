package core.network.messages.s2c;

import core.components.DrawComponent;
import core.components.PositionComponent;
import core.network.messages.NetworkMessage;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public record EntitySpawnEvent(
  int entityId,
  PositionComponent positionComponent,
  DrawComponent drawComponent)
    implements NetworkMessage {
}
