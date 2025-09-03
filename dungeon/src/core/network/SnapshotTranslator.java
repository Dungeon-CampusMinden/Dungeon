package core.network;

import core.Entity;
import core.network.messages.s2c.SnapshotMessage;
import java.util.Map;
import java.util.Optional;

/**
 * Translates between ECS state and {@link SnapshotMessage} wire format.
 *
 * <p>Server-side: build a {@link SnapshotMessage} from authoritative entities. Client-side: apply a
 * {@link SnapshotMessage} by dispatching granular updates via {@link MessageDispatcher}.
 */
public interface SnapshotTranslator {
  /**
   * Server-side translation: build a snapshot for the given tick from authoritative entities.
   *
   * @param serverTick the current server tick
   * @param clientEntities mapping of clientId to authoritative entity (at minimum includes players)
   */
  Optional<SnapshotMessage> translateToSnapshot(
      long serverTick, Map<Integer, Entity> clientEntities);

  /**
   * Client-side application: convert the snapshot into granular updates and dispatch them.
   *
   * <p>Must not manipulate game-thread state directly; only dispatch to the provided dispatcher.
   */
  void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher);
}
