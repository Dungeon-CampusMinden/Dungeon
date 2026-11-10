package rooms.morphco.network;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.DefaultSnapshotTranslator;
import engine.network.MessageDispatcher;
import engine.network.SnapshotTranslator;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.SnapshotMessage;
import feature.collision.CollideSync;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.timer.WorldTimerComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adds collider geometry and interaction reach to the ordinary world snapshots.
 *
 * <p>Clients predict their own movement, so they need the solid colliders of desks and machines.
 * The interaction reach lets them highlight the objects that can be used from where they stand. The
 * shift clock only needs its start; clients count from there on their synchronized play clock.
 */
public final class MorphCoSnapshotTranslator implements SnapshotTranslator {
  private static final CollideSync COLLIDE_SYNC = CollideSync.withPrefix("morphCo.collider");
  private static final String RANGE_KEY = "morphCo.interactionRange";
  private static final String STOPWATCH_KEY = "morphCo.stopwatch";
  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();

  @Override
  public Optional<SnapshotMessage> translateToSnapshot(int serverTick) {
    return delegate
        .translateToSnapshot(serverTick)
        .map(
            snapshot -> {
              List<EntityState> entities = new ArrayList<>();
              for (EntityState state : snapshot.entities()) {
                Optional<Entity> entity =
                    Game.findEntityById(state.entityId())
                        .filter(
                            e ->
                                e.isPresent(PositionComponent.class)
                                    && e.isPresent(DrawComponent.class));
                if (entity.isEmpty()) {
                  entities.add(state);
                  continue;
                }
                Map<String, String> metadata = new HashMap<>();
                float range =
                    entity
                        .get()
                        .fetch(InteractionComponent.class)
                        .map(component -> component.interaction().range())
                        .orElse(0f);
                metadata.put(RANGE_KEY, Float.toString(range));
                entity
                    .get()
                    .fetch(WorldTimerComponent.class)
                    .ifPresent(
                        timer ->
                            metadata.put(STOPWATCH_KEY, Long.toString(timer.startedAtActiveMs())));
                COLLIDE_SYNC.appendMetadata(entity.get(), metadata);
                entities.add(withMergedMetadata(state, metadata));
              }
              return new SnapshotMessage(snapshot.serverTick(), entities, snapshot.levelState());
            });
  }

  @Override
  public void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher) {
    delegate.applySnapshot(snapshot, dispatcher);
    for (EntityState state : snapshot.entities()) {
      state
          .metadata()
          .flatMap(COLLIDE_SYNC::fromMetadata)
          .ifPresent(
              collider ->
                  Game.findEntityById(state.entityId())
                      .ifPresent(entity -> COLLIDE_SYNC.apply(entity, collider)));
      state
          .metadata()
          .map(metadata -> metadata.get(RANGE_KEY))
          .ifPresent(
              value ->
                  Game.findEntityById(state.entityId())
                      .ifPresent(entity -> applyInteractionRange(entity, Float.parseFloat(value))));
      state
          .metadata()
          .map(metadata -> metadata.get(STOPWATCH_KEY))
          .ifPresent(
              value -> {
                WorldTimerComponent received = WorldTimerComponent.stopwatch(Long.parseLong(value));
                Game.findEntityById(state.entityId())
                    .filter(
                        entity ->
                            entity
                                .fetch(WorldTimerComponent.class)
                                .filter(received::equals)
                                .isEmpty())
                    .ifPresent(entity -> entity.add(received));
              });
    }
  }

  private static void applyInteractionRange(Entity entity, float range) {
    if (!Float.isFinite(range) || range < 0) return;
    if (range == 0) {
      entity.remove(InteractionComponent.class);
    } else if (entity
        .fetch(InteractionComponent.class)
        .map(component -> component.interaction().range() != range)
        .orElse(true)) {
      entity.remove(InteractionComponent.class);
      entity.add(new InteractionComponent(new Interaction((target, who) -> {}, range)));
    }
  }

  private static EntityState withMergedMetadata(
      EntityState baseState, Map<String, String> metadata) {
    EntityState.Builder builder = EntityState.builder().entityId(baseState.entityId());
    baseState.entityName().ifPresent(builder::entityName);
    baseState.position().ifPresent(builder::position);
    baseState.viewDirection().ifPresent(builder::viewDirection);
    baseState.rotation().ifPresent(builder::rotation);
    baseState.scale().ifPresent(builder::scale);
    baseState.currentHealth().ifPresent(builder::currentHealth);
    baseState.maxHealth().ifPresent(builder::maxHealth);
    baseState.currentMana().ifPresent(builder::currentMana);
    baseState.maxMana().ifPresent(builder::maxMana);
    baseState.stateName().ifPresent(builder::stateName);
    baseState.tintColor().ifPresent(builder::tintColor);
    baseState.inventory().ifPresent(builder::inventorySlots);
    baseState.shaderComponent().ifPresent(builder::shaderComponent);
    Map<String, String> merged = new HashMap<>();
    baseState.metadata().ifPresent(merged::putAll);
    merged.putAll(metadata);
    builder.metadata(merged);
    return builder.build();
  }
}
