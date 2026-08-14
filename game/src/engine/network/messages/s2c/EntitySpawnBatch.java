package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;
import java.util.List;

/**
 * Server-to-client: batch spawn multiple entities.
 *
 * <p>This message reduces overhead when spawning many entities at once.
 *
 * @param entities the list of entity spawn events
 */
public record EntitySpawnBatch(List<EntitySpawnEvent> entities) implements NetworkMessage {}
