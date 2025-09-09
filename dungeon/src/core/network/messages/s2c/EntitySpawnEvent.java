package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public record EntitySpawnEvent(
    int entityId,
    Point position,
    Direction viewDirection,
    String texturePath,
    String initialState,
    int tintColor)
    implements NetworkMessage {
}
