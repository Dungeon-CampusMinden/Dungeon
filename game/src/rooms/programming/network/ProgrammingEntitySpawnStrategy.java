package rooms.programming.network;

import engine.Entity;
import engine.network.config.DefaultEntitySpawnStrategy;
import engine.network.config.EntitySpawnStrategy;
import engine.network.messages.s2c.EntitySpawnEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import rooms.programming.state.ProgrammingStateComponent;

/** Adds Programming 1 state metadata to entity spawn events. */
public final class ProgrammingEntitySpawnStrategy implements EntitySpawnStrategy {

  private final EntitySpawnStrategy delegate = new DefaultEntitySpawnStrategy();

  @Override
  public Optional<EntitySpawnEvent> buildSpawnEvent(Entity entity) {
    Optional<EntitySpawnEvent> baseEvent = delegate.buildSpawnEvent(entity);
    Optional<ProgrammingStateComponent> roomState = entity.fetch(ProgrammingStateComponent.class);
    if (roomState.isEmpty()) {
      return baseEvent;
    }

    Map<String, String> metadata = new HashMap<>();
    baseEvent.ifPresent(event -> metadata.putAll(event.metadata()));
    metadata.putAll(ProgrammingStateMetadata.encode(roomState.orElseThrow()));

    EntitySpawnEvent.Builder builder =
        EntitySpawnEvent.builder().entityId(entity.id()).metadata(metadata);
    baseEvent.ifPresent(
        event ->
            builder
                .positionComponent(event.positionComponent())
                .drawInfo(event.drawInfo())
                .playerComponent(event.playerComponent())
                .characterClassId(event.characterClassId()));
    return Optional.of(builder.build());
  }
}
