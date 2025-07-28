package core.network.messages;

import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.HashMap;
import java.util.Map;

/** Record representing the state of one or more entities. */
public record EntityStateUpdate(Map<Integer, EntityState> entityStates) implements NetworkMessage {

  public EntityStateUpdate() {
    this(new HashMap<>());
  }

  public void addEntityState(int entityId, EntityState state) {
    entityStates.put(entityId, state);
  }

  public EntityState getEntityState(int entityId) {
    return entityStates.get(entityId);
  }

  public record EntityState(
      Point position,
      Vector2 velocity,
      Direction viewDirection,
      String animationState,
      boolean isVisible,
      int health) {}
}
