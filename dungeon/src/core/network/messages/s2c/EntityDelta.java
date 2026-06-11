package core.network.messages.s2c;

import java.util.Objects;
import java.util.Set;

/**
 * Delta for one entity against a full snapshot baseline.
 *
 * @param entityId entity identifier
 * @param changedState state containing only changed fields
 * @param clearedFields fields that must be removed from the baseline state
 */
public record EntityDelta(
    int entityId, EntityState changedState, Set<EntityStateField> clearedFields) {

  /**
   * Creates an immutable entity delta.
   *
   * @param entityId entity identifier
   * @param changedState state containing only changed fields
   * @param clearedFields fields that must be removed from the baseline state
   */
  public EntityDelta {
    changedState = Objects.requireNonNull(changedState, "changedState");
    clearedFields = Set.copyOf(Objects.requireNonNull(clearedFields, "clearedFields"));
  }
}
