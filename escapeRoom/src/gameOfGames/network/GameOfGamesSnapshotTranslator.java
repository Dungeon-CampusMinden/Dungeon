package gameOfGames.network;

import contrib.components.CollideComponent;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.network.DefaultSnapshotTranslator;
import core.network.MessageDispatcher;
import core.network.SnapshotTranslator;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.SnapshotMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Snapshot translator for Game of Games metadata such as interaction and collider state. */
public final class GameOfGamesSnapshotTranslator implements SnapshotTranslator {

  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();

  /**
   * Builds a snapshot and merges Game of Games metadata into relevant entity states.
   *
   * @param serverTick the current server tick
   * @return a snapshot containing default state plus Game of Games metadata
   */
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
            entity -> {
              Map<String, String> metadata = snapshotMetadata(entity);
              if (metadata.isEmpty()) {
                return;
              }

              int index = indexOfEntityStateById(entities, entity.id()).orElse(-1);
              if (index >= 0) {
                entities.set(index, withMergedMetadata(entities.get(index), metadata));
              } else {
                entities.add(metadataOnlyState(entity, metadata));
              }
            });

    return Optional.of(new SnapshotMessage(snapshot.serverTick(), entities, snapshot.levelState()));
  }

  /**
   * Applies default snapshot behavior and updates local Game of Games metadata-backed components.
   *
   * @param snapshot the received snapshot message
   * @param dispatcher the message dispatcher used by the default translator
   */
  @Override
  public void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher) {
    delegate.applySnapshot(snapshot, dispatcher);

    for (EntityState entityState : snapshot.entities()) {
      Optional<Map<String, String>> metadata = entityState.metadata();
      if (metadata.isEmpty()) {
        continue;
      }
      Game.findEntityById(entityState.entityId())
          .ifPresent(
              entity -> {
                applyInteractableMetadata(entity, metadata.orElseThrow());
                collideComponentFromMetadata(metadata.orElseThrow())
                    .ifPresent(collideState -> GameOfGamesCollideSync.apply(entity, collideState));
              });
    }
  }

  /**
   * Creates a {@link CollideComponent} from metadata.
   *
   * @param metadata the metadata to parse
   * @return the reconstructed collider component, if metadata is present and valid
   */
  public static Optional<CollideComponent> collideComponentFromMetadata(
      Map<String, String> metadata) {
    return GameOfGamesCollideSync.fromMetadata(metadata);
  }

  /**
   * Applies interactable metadata to a client-side entity.
   *
   * @param entity the target entity
   * @param metadata the metadata map
   */
  public static void applyInteractableMetadata(Entity entity, Map<String, String> metadata) {
    String interactable = metadata.get(GameOfGamesEntitySpawnStrategy.METADATA_INTERACTABLE);
    if (interactable == null) {
      return;
    }

    if (Boolean.parseBoolean(interactable)) {
      if (!entity.isPresent(InteractionComponent.class)) {
        entity.add(new InteractionComponent());
      }
    } else {
      entity.remove(InteractionComponent.class);
    }
  }

  private Map<String, String> snapshotMetadata(Entity entity) {
    Map<String, String> metadata = new HashMap<>();
    if (entity.isPresent(PositionComponent.class) && entity.isPresent(DrawComponent.class)) {
      metadata.put(
          GameOfGamesEntitySpawnStrategy.METADATA_INTERACTABLE,
          String.valueOf(entity.isPresent(InteractionComponent.class)));
    }
    GameOfGamesCollideSync.appendMetadata(entity, metadata);
    return metadata;
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

  private EntityState metadataOnlyState(Entity entity, Map<String, String> metadata) {
    EntityState.Builder builder = EntityState.builder().entityId(entity.id()).metadata(metadata);
    if (entity.name() != null && !entity.name().isBlank()) {
      builder.entityName(entity.name());
    }
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            positionComponent -> {
              builder.position(positionComponent.position());
              builder.viewDirection(positionComponent.viewDirection());
              builder.rotation(positionComponent.rotation());
              builder.scale(positionComponent.scale());
            });
    return builder.build();
  }

  private Optional<Integer> indexOfEntityStateById(List<EntityState> entities, int entityId) {
    for (int i = 0; i < entities.size(); i++) {
      if (entities.get(i).entityId() == entityId) {
        return Optional.of(i);
      }
    }
    return Optional.empty();
  }
}
