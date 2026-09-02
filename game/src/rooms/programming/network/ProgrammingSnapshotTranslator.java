package rooms.programming.network;

import engine.Entity;
import engine.Game;
import engine.network.DefaultSnapshotTranslator;
import engine.network.MessageDispatcher;
import engine.network.SnapshotTranslator;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.SnapshotMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.programming.state.ProgrammingStateComponent;

/** Synchronizes the authoritative Programming 1 room state through snapshot metadata. */
public final class ProgrammingSnapshotTranslator implements SnapshotTranslator {

  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();

  @Override
  public Optional<SnapshotMessage> translateToSnapshot(int serverTick) {
    Optional<SnapshotMessage> baseSnapshot = delegate.translateToSnapshot(serverTick);
    if (baseSnapshot.isEmpty()) {
      return Optional.empty();
    }

    SnapshotMessage snapshot = baseSnapshot.orElseThrow();
    List<EntityState> entities = new ArrayList<>(snapshot.entities());
    Game.levelEntities()
        .forEach(
            entity ->
                entity
                    .fetch(ProgrammingStateComponent.class)
                    .ifPresent(
                        state -> {
                          entities.removeIf(entityState -> entityState.entityId() == entity.id());
                          entities.add(stateEntity(entity, state));
                        }));
    return Optional.of(new SnapshotMessage(snapshot.serverTick(), entities, snapshot.levelState()));
  }

  @Override
  public void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher) {
    delegate.applySnapshot(snapshot, dispatcher);
    for (EntityState entityState : snapshot.entities()) {
      Optional<Map<String, String>> metadata = entityState.metadata();
      if (metadata.isEmpty()) {
        continue;
      }
      Optional<ProgrammingStateComponent> state =
          ProgrammingStateMetadata.decode(metadata.orElseThrow());
      if (state.isEmpty()) {
        continue;
      }
      Game.findEntityById(entityState.entityId())
          .ifPresent(
              entity -> {
                entity.remove(ProgrammingStateComponent.class);
                entity.add(state.orElseThrow());
              });
    }
  }

  private EntityState stateEntity(Entity entity, ProgrammingStateComponent state) {
    EntityState.Builder builder =
        EntityState.builder()
            .entityId(entity.id())
            .metadata(ProgrammingStateMetadata.encode(state));
    if (entity.name() != null && !entity.name().isBlank()) {
      builder.entityName(entity.name());
    }
    return builder.build();
  }
}
