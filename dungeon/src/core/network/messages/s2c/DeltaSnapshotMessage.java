package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Server-to-client delta snapshot against a full snapshot baseline.
 *
 * @param baseTick server tick of the baseline full snapshot
 * @param serverTick server tick represented by this delta
 * @param entityDeltas per-entity field deltas
 * @param removedEntityIds entity IDs removed since the baseline
 * @param levelStateDelta optional level-state delta
 */
public record DeltaSnapshotMessage(
    int baseTick,
    int serverTick,
    List<EntityDelta> entityDeltas,
    List<Integer> removedEntityIds,
    LevelState levelStateDelta)
    implements NetworkMessage {

  /**
   * Creates an immutable delta snapshot message.
   *
   * @param baseTick server tick of the baseline full snapshot
   * @param serverTick server tick represented by this delta
   * @param entityDeltas per-entity field deltas
   * @param removedEntityIds entity IDs removed since the baseline
   * @param levelStateDelta optional level-state delta
   */
  public DeltaSnapshotMessage {
    entityDeltas = List.copyOf(Objects.requireNonNull(entityDeltas, "entityDeltas"));
    removedEntityIds = List.copyOf(Objects.requireNonNull(removedEntityIds, "removedEntityIds"));
  }

  /**
   * Returns the optional level-state delta.
   *
   * @return the level-state delta if present
   */
  public Optional<LevelState> levelStateDeltaOptional() {
    return Optional.ofNullable(levelStateDelta);
  }

  /**
   * Returns whether this delta contains any state change.
   *
   * @return true when this delta has entity, removal, or level changes
   */
  public boolean hasChanges() {
    return !entityDeltas.isEmpty() || !removedEntityIds.isEmpty() || levelStateDelta != null;
  }
}
