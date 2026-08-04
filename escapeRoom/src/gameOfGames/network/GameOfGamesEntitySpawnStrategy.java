package gameOfGames.network;

import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.components.PositionComponent;
import core.network.config.DefaultEntitySpawnStrategy;
import core.network.config.EntitySpawnStrategy;
import core.network.messages.s2c.EntitySpawnEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Entity spawn strategy for Game of Games-specific spawn metadata. */
public final class GameOfGamesEntitySpawnStrategy implements EntitySpawnStrategy {

  /** Metadata key indicating whether an entity should be treated as interactable on clients. */
  public static final String METADATA_INTERACTABLE = "gog.interactable";

  private final EntitySpawnStrategy delegate = new DefaultEntitySpawnStrategy();

  /**
   * Builds a spawn event and appends Game of Games metadata where needed.
   *
   * @param entity the source entity
   * @return a spawn event when the entity is network relevant
   */
  @Override
  public Optional<EntitySpawnEvent> buildSpawnEvent(Entity entity) {
    Optional<EntitySpawnEvent> defaultSpawn = delegate.buildSpawnEvent(entity);
    Map<String, String> metadata = new HashMap<>();
    defaultSpawn.ifPresent(spawnEvent -> metadata.putAll(spawnEvent.metadata()));

    if (entity.isPresent(InteractionComponent.class)) {
      metadata.put(METADATA_INTERACTABLE, String.valueOf(true));
    }
    GameOfGamesCollideSync.appendMetadata(entity, metadata);

    if (defaultSpawn.isPresent()) {
      EntitySpawnEvent base = defaultSpawn.orElseThrow();
      if (metadata.isEmpty()) {
        return defaultSpawn;
      }
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
