package rooms.programming.network;

import engine.Entity;
import engine.components.PositionComponent;
import engine.network.config.DefaultEntitySpawnStrategy;
import engine.network.config.EntitySpawnStrategy;
import engine.network.messages.s2c.EntitySpawnEvent;
import feature.interaction.InteractionComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import rooms.programming.state.ProgrammingStateComponent;

/** Adds Programming 1 state metadata to entity spawn events. */
public final class ProgrammingEntitySpawnStrategy implements EntitySpawnStrategy {

  static final String INTERACTABLE_KEY = "programming.interactable";

  private final EntitySpawnStrategy delegate = new DefaultEntitySpawnStrategy();

  @Override
  public Optional<EntitySpawnEvent> buildSpawnEvent(Entity entity) {
    Optional<EntitySpawnEvent> baseEvent = delegate.buildSpawnEvent(entity);
    Map<String, String> metadata = new HashMap<>();
    baseEvent.ifPresent(event -> metadata.putAll(event.metadata()));
    entity
        .fetch(ProgrammingStateComponent.class)
        .ifPresent(state -> metadata.putAll(ProgrammingStateMetadata.encode(state)));
    if (entity.isPresent(InteractionComponent.class)) {
      metadata.put(INTERACTABLE_KEY, String.valueOf(true));
    }

    if (metadata.isEmpty()) {
      return baseEvent;
    }

    EntitySpawnEvent.Builder builder =
        EntitySpawnEvent.builder().entityId(entity.id()).metadata(metadata);
    baseEvent.ifPresent(
        event ->
            builder
                .positionComponent(event.positionComponent())
                .drawInfo(event.drawInfo())
                .playerComponent(event.playerComponent())
                .characterClassId(event.characterClassId()));
    if (baseEvent.isEmpty()) {
      entity.fetch(PositionComponent.class).ifPresent(builder::positionComponent);
    }
    return Optional.of(builder.build());
  }
}
