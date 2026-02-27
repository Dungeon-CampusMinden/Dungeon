package network;

import core.Entity;
import core.network.config.DefaultEntitySpawnStrategy;
import core.network.config.EntitySpawnStrategy;
import core.network.messages.s2c.EntitySpawnEvent;
import java.util.Map;
import java.util.Optional;
import modules.computer.ComputerStateComponent;

/**
 * Entity spawn strategy for The Last Hour that supports metadata-only spawn events for {@link
 * ComputerStateComponent} entities.
 */
public final class LastHourEntitySpawnStrategy implements EntitySpawnStrategy {

  private static final String METADATA_PROGRESS = "progress";
  private static final String METADATA_INFECTED = "isInfected";
  private static final String METADATA_VIRUS_TYPE = "virusType";

  private final EntitySpawnStrategy delegate = new DefaultEntitySpawnStrategy();

  /**
   * Builds a spawn event using default behavior first and falls back to metadata-only events for
   * computer-state entities.
   *
   * @param entity the source entity
   * @return an Optional containing a spawn event if the entity is spawnable, otherwise empty
   */
  @Override
  public Optional<EntitySpawnEvent> buildSpawnEvent(Entity entity) {
    Optional<EntitySpawnEvent> defaultSpawn = delegate.buildSpawnEvent(entity);
    if (defaultSpawn.isPresent()) {
      return defaultSpawn;
    }

    return entity
        .fetch(ComputerStateComponent.class)
        .map(
            state ->
                EntitySpawnEvent.builder()
                    .entityId(entity.id())
                    .isPersistent(false)
                    .metadata(
                        Map.of(
                            METADATA_PROGRESS,
                            state.state().name(),
                            METADATA_INFECTED,
                            String.valueOf(state.isInfected()),
                            METADATA_VIRUS_TYPE,
                            state.virusType() == null ? "" : state.virusType()))
                    .build());
  }
}
