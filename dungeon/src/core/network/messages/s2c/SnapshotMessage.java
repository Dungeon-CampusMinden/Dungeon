package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import java.util.List;
import java.util.Objects;

/**
 * Server→client: compact snapshot of world state for a frame/tick.
 *
 * <p>Each snapshot contains the states of all relevant entities at a specific tick. In multiplayer,
 * the server sends this full snapshot reliably for initial sync, level changes, and recovery
 * baselines.
 *
 * @param serverTick optional monotonic tick number assigned by server
 * @param entities list of entity states
 * @param levelState optional level state snapshot
 * @see EntityState
 */
public record SnapshotMessage(int serverTick, List<EntityState> entities, LevelState levelState)
    implements NetworkMessage {

  /**
   * Creates an immutable snapshot message.
   *
   * @param serverTick optional monotonic tick number assigned by server
   * @param entities list of entity states
   * @param levelState optional level state snapshot
   */
  public SnapshotMessage {
    entities = List.copyOf(Objects.requireNonNull(entities, "entities"));
  }
}
