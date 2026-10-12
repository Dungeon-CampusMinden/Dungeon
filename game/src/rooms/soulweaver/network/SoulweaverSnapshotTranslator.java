package rooms.soulweaver.network;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.network.DefaultSnapshotTranslator;
import engine.network.MessageDispatcher;
import engine.network.SnapshotTranslator;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.SnapshotMessage;
import feature.collision.CollideSync;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import rooms.soulweaver.level.SoulweaverBinding;
import rooms.soulweaver.level.SoulweaverDecisions;
import rooms.soulweaver.level.SoulweaverHelp;
import rooms.soulweaver.level.SoulweaverMethods;
import rooms.soulweaver.level.SoulweaverTerminal;

/** Adds collider geometry, interaction reach and room state to ordinary world snapshots. */
public final class SoulweaverSnapshotTranslator implements SnapshotTranslator {
  private static final CollideSync COLLIDE_SYNC = CollideSync.withPrefix("soulweaver.collider");
  private static final String RANGE_KEY = "soulweaver.interactionRange";
  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();
  private final Map<String, EncodedState> roomStates = new HashMap<>();
  private ILevel cachedLevel;

  private record EncodedState(Object state, String value) {}

  /**
   * Keeps only the most recent immutable value for each room-state channel.
   *
   * @param <T> immutable state type
   * @param key room-state channel
   * @param state current authoritative state
   * @param encoder state serialization
   * @return cached or newly encoded state
   */
  private <T> String encode(String key, T state, Function<T, String> encoder) {
    EncodedState previous = roomStates.get(key);
    if (previous != null && previous.state().equals(state)) return previous.value();
    String value = encoder.apply(state);
    roomStates.put(key, new EncodedState(state, value));
    return value;
  }

  @Override
  public Optional<SnapshotMessage> translateToSnapshot(int serverTick) {
    ILevel level = Game.currentLevel().orElse(null);
    if (level != cachedLevel) {
      roomStates.clear();
      cachedLevel = level;
    }
    return delegate
        .translateToSnapshot(serverTick)
        .map(
            snapshot -> {
              List<EntityState> entities = new ArrayList<>();
              var terminal = SoulweaverTerminal.state();
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
                  COLLIDE_SYNC.appendMetadata(entity.orElseThrow(), metadata);
                  terminal
                      .filter(s -> s.golemId() == state.entityId())
                      .ifPresent(
                          s -> {
                            metadata.put(
                                "soulweaver.terminal",
                                encode("soulweaver.terminal", s, SoulweaverTerminal::encode));
                            SoulweaverHelp.state()
                                .ifPresent(
                                    help ->
                                        metadata.put(
                                            SoulweaverHelp.ID,
                                            encode(
                                                SoulweaverHelp.ID, help, SoulweaverHelp::encode)));
                            if (s.finished())
                              SoulweaverMethods.state()
                                  // The opening dialog carries the untouched starting program.
                                  .filter(methods -> methods.revision() > 0)
                                  .ifPresent(
                                      methods ->
                                          metadata.put(
                                              "soulweaver.methods",
                                              encode(
                                                  "soulweaver.methods",
                                                  methods,
                                                  SoulweaverMethods::encode)));
                            SoulweaverDecisions.state()
                                .ifPresent(
                                    decisions ->
                                        metadata.put(
                                            SoulweaverDecisions.ID,
                                            encode(
                                                SoulweaverDecisions.ID,
                                                decisions,
                                                SoulweaverDecisions::encode)));
                            SoulweaverBinding.state()
                                .ifPresent(
                                    binding ->
                                        metadata.put(
                                            "soulweaver.binding",
                                            encode(
                                                "soulweaver.binding",
                                                binding,
                                                SoulweaverBinding::encode)));
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
    for (EntityState state : snapshot.entities())
      state
          .metadata()
          .map(metadata -> metadata.get(SoulweaverDecisions.ID))
          .ifPresent(SoulweaverDecisions::receive);
    for (EntityState state : snapshot.entities()) {
      state
          .metadata()
          .map(metadata -> metadata.get(SoulweaverHelp.ID))
          .ifPresent(SoulweaverHelp::receive);
      state
          .metadata()
          .map(metadata -> metadata.get("soulweaver.methods"))
          .ifPresent(SoulweaverMethods::receive);
      state
          .metadata()
          .flatMap(COLLIDE_SYNC::fromMetadata)
          .ifPresent(
              collider ->
                  Game.findEntityById(state.entityId())
                      .ifPresent(entity -> COLLIDE_SYNC.apply(entity, collider)));
      state
          .metadata()
          .map(metadata -> metadata.get("soulweaver.binding"))
          .ifPresent(SoulweaverBinding::receive);
      state
          .metadata()
          .map(metadata -> metadata.get("soulweaver.terminal"))
          .ifPresent(SoulweaverTerminal::receive);
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
    baseState.shaderComponent().ifPresent(builder::shaderComponent);

    Map<String, String> mergedMetadata = new HashMap<>();
    baseState.metadata().ifPresent(mergedMetadata::putAll);
    mergedMetadata.putAll(metadata);
    builder.metadata(mergedMetadata);
    return builder.build();
  }
}
