package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import java.util.List;

/**
 * Server→client: compact snapshot of world state for a frame/tick.
 *
 * <p>Temporary Java-serialized prototype to be replaced by protobuf later. Expected max size:
 * depends on entity count; keep small for UDP when used.
 *
 * @param serverTick optional monotonic tick
 */
public record SnapshotMessage(long serverTick, List<EntityState> entities)
    implements NetworkMessage {}
