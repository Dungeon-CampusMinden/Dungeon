package core.network.messages.s2c;

import core.Entity;
import core.components.PositionComponent;
import core.network.messages.NetworkMessage;
import core.network.server.ClientState;
import core.utils.Point;
import java.util.List;
import java.util.Optional;

/**
 * Server→client: compact snapshot of world state for a frame/tick.
 *
 * <p>Each snapshot contains the states of all relevant entities at a specific tick. This message is
 * sent periodically from the server to clients to synchronize their game state.
 *
 * @param serverTick optional monotonic tick number assigned by server
 * @param entities list of entity states
 * @param levelState the current state of the level
 * @see EntityState
 */
public record SnapshotMessage(int serverTick, List<EntityState> entities, LevelState levelState)
    implements NetworkMessage {

  /**
   * Filters the snapshot message to include only entities relevant to the specified recipient
   * client state.
   *
   * <p>This method checks the position of each entity and determines if it is near the player
   * associated with the recipient client state. Only entities that are considered relevant (i.e.,
   * near the player) are included in the returned SnapshotMessage.
   *
   * @param recipient the client state of the recipient
   * @return a new SnapshotMessage containing only relevant entities for the recipient
   */
  public SnapshotMessage filterForRecipient(ClientState recipient) {
    List<EntityState> filteredEntities =
        entities.stream()
            .filter(entityState -> isNearPlayer(entityState.position().orElse(null), recipient))
            .toList();

    return new SnapshotMessage(serverTick, filteredEntities, levelState);
  }

  /**
   * Determines if an entity at the given position is near the player associated with the recipient
   * client state.
   *
   * @param position the position of the entity
   * @param recipient the client state of the recipient
   * @return true if the entity is near the player, false otherwise
   */
  private boolean isNearPlayer(Point position, ClientState recipient) {
    final double RELEVANCE_DISTANCE = 20.0;

    Optional<Entity> playerEntityOpt = recipient.playerEntity();
    if (playerEntityOpt.isEmpty()) {
      return true; // No player entity associated, consider all entities relevant
    }
    Entity playerEntity = playerEntityOpt.get();

    Optional<PositionComponent> playerPosCompOpt = playerEntity.fetch(PositionComponent.class);
    if (playerPosCompOpt.isEmpty() || position == null) {
      return true; // Missing position component, consider relevant
    }
    PositionComponent playerPosComp = playerPosCompOpt.get();

    double distance = playerPosComp.position().distance(position);
    return distance <= RELEVANCE_DISTANCE;
  }
}
