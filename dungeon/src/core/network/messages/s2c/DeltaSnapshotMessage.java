package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import java.io.Serial;
import java.util.List;

/**
 * Server→client: delta snapshot containing only changed entities since the last full snapshot.
 *
 * <p>This message is used to efficiently synchronize game state by sending only entities that have
 * changed since the last full snapshot. It also includes a list of entity IDs that should be
 * removed from the client (entities that left the client's visibility range).
 *
 * <p>Delta snapshots are sent at a high frequency via UDP, while full snapshots are sent
 * periodically via TCP to ensure eventual consistency.
 *
 * @param baseTick the server tick of the full snapshot this delta is based on
 * @param serverTick the current server tick for this delta
 * @param changedEntities list of entity states that have changed since the last snapshot
 * @param removedEntityIds list of entity IDs that should be removed (left visibility range)
 * @param deltaLevelState the level state delta (only changed doors/tiles), may be null if no
 *     changes
 * @see SnapshotMessage
 * @see EntityState
 */
public record DeltaSnapshotMessage(
    int baseTick,
    int serverTick,
    List<EntityState> changedEntities,
    List<Integer> removedEntityIds,
    LevelState deltaLevelState)
    implements NetworkMessage {

  @Serial private static final long serialVersionUID = 1L;

  /**
   * Returns true if this delta snapshot has any changes to send.
   *
   * @return true if there are changed entities, removed entities, or level changes
   */
  public boolean hasChanges() {
    boolean hasEntityChanges = changedEntities != null && !changedEntities.isEmpty();
    boolean hasRemovals = removedEntityIds != null && !removedEntityIds.isEmpty();
    boolean hasLevelChanges = deltaLevelState != null && !deltaLevelState.isEmpty();
    return hasEntityChanges || hasRemovals || hasLevelChanges;
  }
}
