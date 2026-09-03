package rooms.systemRecovery.network;

import engine.Entity;
import engine.components.PositionComponent;
import engine.network.config.DefaultEntitySpawnStrategy;
import engine.network.config.EntitySpawnStrategy;
import engine.network.messages.s2c.EntitySpawnEvent;
import feature.interaction.InteractionComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Entity spawn strategy for System Recovery metadata. */
public final class SystemRecoveryEntitySpawnStrategy implements EntitySpawnStrategy {

  /** Metadata key identifying the custom entity type. */
  public static final String METADATA_TYPE = "systemRecovery.type";

  /** Metadata key indicating whether the entity is interactable. */
  public static final String METADATA_INTERACTABLE = "systemRecovery.interactable";

  private final EntitySpawnStrategy delegate = new DefaultEntitySpawnStrategy();

  /**
   * Builds a spawn event using default behavior and appends System Recovery metadata where needed.
   *
   * @param entity the source entity
   * @return an Optional containing a spawn event if the entity is spawnable, otherwise empty
   */
  @Override
  public Optional<EntitySpawnEvent> buildSpawnEvent(Entity entity) {
    Optional<EntitySpawnEvent> defaultSpawn = delegate.buildSpawnEvent(entity);
    Map<String, String> metadata = new HashMap<>();
    defaultSpawn.ifPresent(spawnEvent -> metadata.putAll(spawnEvent.metadata()));

    entity
        .fetch(InteractionComponent.class)
        .ifPresent(interaction -> metadata.put(METADATA_INTERACTABLE, String.valueOf(true)));
    SystemRecoveryCollideSync.appendMetadata(entity, metadata);

    if (defaultSpawn.isPresent() && !metadata.isEmpty()) {
      EntitySpawnEvent base = defaultSpawn.orElseThrow();
      return Optional.of(
          EntitySpawnEvent.builder()
              .entityId(base.entityId())
              .positionComponent(base.positionComponent())
              .drawInfo(base.drawInfo())
              .playerComponent(base.playerComponent())
              .characterClassId(base.characterClassId())
              .metadata(metadata)
              .build());
    }

    if (defaultSpawn.isPresent()) {
      return defaultSpawn;
    }

    if (metadata.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        EntitySpawnEvent.builder()
            .entityId(entity.id())
            .positionComponent(entity.fetch(PositionComponent.class).orElse(null))
            .metadata(metadata)
            .build());
  }
}
