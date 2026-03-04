package core.network.config;

import core.Entity;
import core.network.messages.s2c.EntitySpawnEvent;
import java.util.Optional;

/**
 * Strategy for building {@link EntitySpawnEvent} from an authoritative server-side entity.
 *
 * <p>Subprojects can replace the default strategy to support custom spawn behavior for entities
 * that do not follow the default requirements.
 */
@FunctionalInterface
public interface EntitySpawnStrategy {

  /**
   * Builds an entity spawn event for the given entity.
   *
   * @param entity the source entity
   * @return an Optional containing a spawn event if the entity is eligible, otherwise empty
   */
  Optional<EntitySpawnEvent> buildSpawnEvent(Entity entity);
}
