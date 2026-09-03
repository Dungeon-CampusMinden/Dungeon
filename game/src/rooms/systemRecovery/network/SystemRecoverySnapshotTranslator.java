package rooms.systemRecovery.network;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.DefaultSnapshotTranslator;
import engine.network.MessageDispatcher;
import engine.network.SnapshotTranslator;
import engine.network.messages.s2c.EntityState;
import engine.network.messages.s2c.SnapshotMessage;
import engine.utils.logging.DungeonLogger;
import feature.components.CollideComponent;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Snapshot translator for metadata-backed System Recovery components. */
public final class SystemRecoverySnapshotTranslator implements SnapshotTranslator {

  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(SystemRecoverySnapshotTranslator.class);

  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();

  /**
   * Builds a snapshot and appends basic-room metadata for shared components.
   *
   * @param serverTick the current server tick
   * @return a snapshot including custom metadata state when available
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
   * Applies default snapshot behavior and updates local metadata-backed components.
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
                keypadStateFromMetadata(metadata.orElseThrow())
                    .ifPresent(keypadState -> applyKeypadState(entity, keypadState));
                collideComponentFromMetadata(metadata.orElseThrow())
                    .ifPresent(collideState -> SystemRecoveryCollideSync.apply(entity, collideState));
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
    return SystemRecoveryCollideSync.fromMetadata(metadata);
  }

  /**
   * Applies interactable metadata to a client-side entity.
   *
   * @param entity the target entity
   * @param metadata the metadata map
   */
  public static void applyInteractableMetadata(Entity entity, Map<String, String> metadata) {
    String interactable = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_INTERACTABLE);
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

  /**
   * Creates a KeypadComponent from metadata if the payload describes a keypad.
   *
   * @param metadata the metadata to parse
   * @return the reconstructed keypad component, if metadata is present and valid
   */
  public static Optional<KeypadComponent> keypadStateFromMetadata(Map<String, String> metadata) {
    if (!SystemRecoveryEntitySpawnStrategy.TYPE_KEYPAD.equals(
        metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_TYPE))) {
      return Optional.empty();
    }

    String correctDigitsRaw =
        metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_CORRECT_DIGITS);
    if (correctDigitsRaw == null) {
      return Optional.empty();
    }

    List<Integer> correctDigits = parseDigits(correctDigitsRaw);
    List<Integer> enteredDigits =
        parseDigits(
            metadata.getOrDefault(
                SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_ENTERED_DIGITS, ""));
    boolean isUnlocked =
        Boolean.parseBoolean(
            metadata.getOrDefault(
                SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_UNLOCKED, "false"));
    boolean showDigitCount =
        Boolean.parseBoolean(
            metadata.getOrDefault(
                SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_SHOW_DIGIT_COUNT, "true"));

    return Optional.of(
        new KeypadComponent(correctDigits, enteredDigits, isUnlocked, showDigitCount));
  }

  private Map<String, String> snapshotMetadata(Entity entity) {
    Map<String, String> metadata = new HashMap<>();
    entity
        .fetch(KeypadComponent.class)
        .ifPresent(keypad -> metadata.putAll(keypadMetadata(keypad)));
    if (entity.isPresent(PositionComponent.class) && entity.isPresent(DrawComponent.class)) {
      metadata.put(
          SystemRecoveryEntitySpawnStrategy.METADATA_INTERACTABLE,
          String.valueOf(entity.isPresent(InteractionComponent.class)));
    }
    SystemRecoveryCollideSync.appendMetadata(entity, metadata);
    return metadata;
  }

  private Map<String, String> keypadMetadata(KeypadComponent keypad) {
    return Map.of(
        SystemRecoveryEntitySpawnStrategy.METADATA_TYPE,
        SystemRecoveryEntitySpawnStrategy.TYPE_KEYPAD,
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_CORRECT_DIGITS,
        digitsToString(keypad.correctDigits()),
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_ENTERED_DIGITS,
        digitsToString(keypad.enteredDigits()),
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_UNLOCKED,
        String.valueOf(keypad.isUnlocked()),
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_SHOW_DIGIT_COUNT,
        String.valueOf(keypad.showDigitCount()));
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

  private void applyKeypadState(Entity entity, KeypadComponent keypadComponent) {
    KeypadComponent component =
        entity
            .fetch(KeypadComponent.class)
            .orElseGet(
                () -> {
                  KeypadComponent newComponent =
                      new KeypadComponent(
                          keypadComponent.correctDigits(),
                          () -> {},
                          keypadComponent.showDigitCount());
                  entity.add(newComponent);
                  return newComponent;
                });
    component.enteredDigits().clear();
    component.enteredDigits().addAll(keypadComponent.enteredDigits());
    component.isUnlocked(keypadComponent.isUnlocked());
    component.showDigitCount(keypadComponent.showDigitCount());
  }

  private static List<Integer> parseDigits(String value) {
    if (value == null || value.isBlank()) {
      return new ArrayList<>();
    }
    try {
      return Stream.of(value.split(","))
          .map(String::trim)
          .map(Integer::parseInt)
          .collect(Collectors.toCollection(ArrayList::new));
    } catch (NumberFormatException ex) {
      LOGGER.warn("Invalid keypad digits metadata '{}'", value);
      return new ArrayList<>();
    }
  }

  private String digitsToString(List<Integer> digits) {
    return digits.stream().map(String::valueOf).collect(Collectors.joining(","));
  }
}
