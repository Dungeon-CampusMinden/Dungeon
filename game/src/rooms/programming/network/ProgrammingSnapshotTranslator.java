package rooms.programming.network;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.DefaultSnapshotTranslator;
import engine.network.MessageDispatcher;
import engine.network.SnapshotTranslator;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.SnapshotMessage;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.programming.level.ProgrammingBinding;
import rooms.programming.level.ProgrammingTerminal;

/** Adds interaction reach to ordinary world snapshots; room progress stays on the server. */
public final class ProgrammingSnapshotTranslator implements SnapshotTranslator {
  private static final String RANGE_KEY = "programming.interactionRange";
  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();

  @Override
  public Optional<SnapshotMessage> translateToSnapshot(int serverTick) {
    return delegate
        .translateToSnapshot(serverTick)
        .map(
            snapshot -> {
              List<EntityState> entities = new ArrayList<>();
              var terminal = ProgrammingTerminal.state();
              for (EntityState state : snapshot.entities()) {
                var entity =
                    Game.findEntityById(state.entityId())
                        .filter(
                            e ->
                                e.isPresent(PositionComponent.class)
                                    && e.isPresent(DrawComponent.class));
                if (entity.isPresent()) {
                  float range =
                      entity
                          .orElseThrow()
                          .fetch(InteractionComponent.class)
                          .map(component -> component.interaction().range())
                          .orElse(0f);
                  Map<String, String> metadata = new HashMap<>();
                  metadata.put(RANGE_KEY, Float.toString(range));
                  terminal
                      .filter(s -> s.golemId() == state.entityId())
                      .ifPresent(
                          s -> {
                            metadata.put("programming.terminal", ProgrammingTerminal.encode(s));
                            ProgrammingBinding.state()
                                .ifPresent(
                                    binding ->
                                        metadata.put(
                                            "programming.binding",
                                            ProgrammingBinding.encode(binding)));
                          });
                  entities.add(withMergedMetadata(state, metadata));
                } else entities.add(state);
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
          .map(metadata -> metadata.get("programming.binding"))
          .ifPresent(ProgrammingBinding::receive);
      state
          .metadata()
          .map(metadata -> metadata.get("programming.terminal"))
          .ifPresent(ProgrammingTerminal::receive);
      state
          .metadata()
          .map(metadata -> metadata.get(RANGE_KEY))
          .ifPresent(
              value ->
                  Game.findEntityById(state.entityId())
                      .ifPresent(entity -> applyInteractionRange(entity, Float.parseFloat(value))));
    }
  }

  private void applyInteractionRange(Entity entity, float range) {
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

  private EntityState withMergedMetadata(EntityState baseState, Map<String, String> metadata) {
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

    Map<String, String> mergedMetadata = new HashMap<>();
    baseState.metadata().ifPresent(mergedMetadata::putAll);
    mergedMetadata.putAll(metadata);
    builder.metadata(mergedMetadata);
    return builder.build();
  }
}
