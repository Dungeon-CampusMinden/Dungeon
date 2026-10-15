package engine.network.config;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.messages.s2c.EntitySpawnEvent;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.TextKeypadSync;
import feature.puzzle.PuzzleNetworkSync;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Default entity spawn strategy.
 *
 * <p>Only entities with both {@link PositionComponent} and {@link DrawComponent} are eligible for
 * spawn events.
 */
public final class DefaultEntitySpawnStrategy implements EntitySpawnStrategy {
  /** Generic marker for entities whose authoritative side has an interaction component. */
  public static final String METADATA_INTERACTABLE = "entity.interactable";

  /**
   * Builds a spawn event from an entity if all required components are present.
   *
   * @param entity the source entity
   * @return an Optional containing the spawn event or empty if the entity is not eligible
   */
  @Override
  public Optional<EntitySpawnEvent> buildSpawnEvent(Entity entity) {
    if (entity.isLocal()
        || !entity.isPresent(PositionComponent.class)
        || !entity.isPresent(DrawComponent.class)) {
      return Optional.empty();
    }
    EntitySpawnEvent base = new EntitySpawnEvent(entity);
    Map<String, String> metadata = new HashMap<>(PuzzleNetworkSync.metadata(entity));
    metadata.putAll(TextKeypadSync.metadata(entity));
    if (entity.isPresent(InteractionComponent.class)) {
      metadata.put(METADATA_INTERACTABLE, "true");
    }
    if (metadata.isEmpty()) return Optional.of(base);
    return Optional.of(
        EntitySpawnEvent.builder()
            .entityId(base.entityId())
            .positionComponent(base.positionComponent())
            .drawInfo(base.drawInfo())
            .playerComponent(base.playerComponent())
            .characterClassId(base.characterClassId())
            .shaderComponent(base.shaderComponent())
            .metadata(metadata)
            .build());
  }
}
