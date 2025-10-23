package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import java.util.List;

/**
 * Server→client: compact snapshot of world state for a frame/tick.
 *
 * <p>Each snapshot contains the states of all relevant entities at a specific tick. This message is
 * sent periodically from the server to clients to synchronize their game state.
 *
 * @param serverTick optional monotonic tick number assigned by server
 * @param entities list of entity states
 * @see EntityState
 */
public record SnapshotMessage(int serverTick, List<EntityState> entities)
    implements NetworkMessage {}
