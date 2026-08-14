package engine.network.config;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.messages.s2c.EntitySpawnEvent;
import java.util.Optional;

/**
 * Default entity spawn strategy.
 *
 * <p>Only entities with both {@link PositionComponent} and {@link DrawComponent} are eligible for
 * spawn events.
 */
public final class DefaultEntitySpawnStrategy implements EntitySpawnStrategy {

  /**
   * Builds a spawn event from an entity if all required components are present.
   *
   * @param entity the source entity
   * @return an Optional containing the spawn event or empty if the entity is not eligible
   */
  @Override
  public Optional<EntitySpawnEvent> buildSpawnEvent(Entity entity) {
    if (!entity.isPresent(PositionComponent.class) || !entity.isPresent(DrawComponent.class)) {
      return Optional.empty();
    }
    return Optional.of(new EntitySpawnEvent(entity));
  }
}
