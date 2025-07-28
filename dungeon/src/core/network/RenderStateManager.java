package core.network;

import core.network.messages.EntityStateUpdate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class RenderStateManager {
  private static final Map<Integer, EntityStateUpdate.EntityState> renderableEntityStates = new ConcurrentHashMap<>();

  public static Map<Integer, EntityStateUpdate.EntityState> renderableEntityStates() {
    return renderableEntityStates;
  }

  // Method for the network listener to update the store
  public static void applyStateUpdate(EntityStateUpdate update) {
    update.entityStates().forEach((id, state) -> {

      if (state == null) {
        // If state is null, we might want to remove the entity from the map
        removeEntityState(id);
        return;
      }
      renderableEntityStates.put(id, state);
    });
  }

  // Method to remove entities (on despawn event)
  public static void removeEntityState(int entityId) {
    renderableEntityStates.remove(entityId);
  }
}
