package core.network;

import core.network.messages.s2c.SnapshotMessage;
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
   * @return an Optional containing the SnapshotMessage if there are updates, or empty if no new
   *     updates are available
   */
  Optional<SnapshotMessage> translateToSnapshot(int serverTick);

  /**
   * Client-side application: convert the snapshot into granular updates and dispatch them.
   *
   * <p>Must not manipulate game-thread state directly; only dispatch to the provided dispatcher.
   *
   * @param snapshot the received snapshot message
   * @param dispatcher the message dispatcher to handle granular updates
   */
  void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher);
}
