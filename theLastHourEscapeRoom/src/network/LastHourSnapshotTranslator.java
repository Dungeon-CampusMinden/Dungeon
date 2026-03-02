package network;

import contrib.modules.keypad.KeypadComponent;
import contrib.modules.worldTimer.WorldTimerComponent;
import core.Entity;
import core.Game;
import core.game.ECSManagement;
import core.network.DefaultSnapshotTranslator;
import core.network.MessageDispatcher;
import core.network.SnapshotTranslator;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.SnapshotMessage;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import modules.computer.ComputerProgress;
import modules.computer.ComputerStateComponent;

/**
 * Snapshot translator for The Last Hour that synchronizes the shared {@link ComputerStateComponent}
 * through metadata.
 */
public final class LastHourSnapshotTranslator implements SnapshotTranslator {

  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(LastHourSnapshotTranslator.class);

  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();

  /**
   * Builds a snapshot and appends the computer state entity as metadata-only state.
   *
   * @param serverTick the current server tick
   * @return a snapshot including computer metadata state when available
   */
  @Override
  public Optional<SnapshotMessage> translateToSnapshot(int serverTick) {
    Optional<SnapshotMessage> baseSnapshot = delegate.translateToSnapshot(serverTick);
    if (baseSnapshot.isEmpty()) {
      return Optional.empty();
    }

    SnapshotMessage snapshot = baseSnapshot.orElseThrow();
    List<EntityState> entities = new ArrayList<>(snapshot.entities());

    ECSManagement.levelEntities(Set.of(ComputerStateComponent.class))
        .findFirst()
        .flatMap(
            entity ->
                entity.fetch(ComputerStateComponent.class).map(state -> Map.entry(entity, state)))
        .ifPresent(
            entry -> {
              Entity entity = entry.getKey();
              entities.removeIf(existing -> existing.entityId() == entity.id());
              entities.add(computerState(entity, entry.getValue()));
            });

    ECSManagement.levelEntities(Set.of(KeypadComponent.class))
        .forEach(
            keypadEntity -> {
              KeypadComponent keypad = keypadEntity.fetch(KeypadComponent.class).orElseThrow();
              int index = indexOfEntityStateById(entities, keypadEntity.id()).orElse(-1);
              if (index >= 0) {
                EntityState baseState = entities.get(index);
                entities.set(index, withMergedMetadata(baseState, keypadMetadata(keypad)));
              }
            });

    return Optional.of(new SnapshotMessage(snapshot.serverTick(), entities));
  }

  /**
   * Applies the base snapshot behavior and updates local computer state entities from metadata.
   *
   * @param snapshot the received snapshot message
   * @param dispatcher the message dispatcher used by the default translator
   */
  @Override
  public void applySnapshot(SnapshotMessage snapshot, MessageDispatcher dispatcher) {
    delegate.applySnapshot(snapshot, dispatcher);

    for (EntityState entityState : snapshot.entities()) {
      Optional<WorldTimerComponent> mappedWorldTimerState =
          entityState.metadata().flatMap(LastHourSnapshotTranslator::worldTimerStateFromMetadata);
      if (mappedWorldTimerState.isPresent()) {
        WorldTimerComponent worldTimerState = mappedWorldTimerState.orElseThrow();
        Game.findEntityById(entityState.entityId())
            .ifPresent(
                entity -> {
                  entity.remove(WorldTimerComponent.class);
                  entity.add(worldTimerState);
                });
        continue;
      }

      Optional<KeypadComponent> mappedKeypadState =
          entityState.metadata().flatMap(LastHourSnapshotTranslator::keypadStateFromMetadata);
      if (mappedKeypadState.isPresent()) {
        KeypadComponent keypadState = mappedKeypadState.orElseThrow();
        Game.findEntityById(entityState.entityId())
            .ifPresent(entity -> applyKeypadState(entity, keypadState));
        continue;
      }

      Optional<ComputerStateComponent> mappedState =
          entityState.metadata().flatMap(LastHourSnapshotTranslator::computerStateFromMetadata);
      if (mappedState.isEmpty()) {
        continue;
      }

      ComputerStateComponent computerState = mappedState.orElseThrow();
      Game.findEntityById(entityState.entityId())
          .ifPresent(
              entity -> {
                entity.remove(ComputerStateComponent.class);
                entity.add(computerState);
              });
    }
  }

  private EntityState computerState(Entity entity, ComputerStateComponent state) {
    return EntityState.builder()
        .entityId(entity.id())
        .entityName(entity.name())
        .metadata(
            Map.of(
                LastHourEntitySpawnStrategy.METADATA_TYPE,
                LastHourEntitySpawnStrategy.TYPE_COMPUTER,
                LastHourEntitySpawnStrategy.METADATA_PROGRESS,
                state.state().name(),
                LastHourEntitySpawnStrategy.METADATA_INFECTED,
                String.valueOf(state.isInfected()),
                LastHourEntitySpawnStrategy.METADATA_VIRUS_TYPE,
                state.virusType() == null ? "" : state.virusType()))
        .build();
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
    baseState.inventory().ifPresent(items -> builder.inventory(items.clone()));

    Map<String, String> mergedMetadata = new HashMap<>();
    baseState.metadata().ifPresent(mergedMetadata::putAll);
    mergedMetadata.putAll(metadata);
    builder.metadata(mergedMetadata);
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

  private Map<String, String> keypadMetadata(KeypadComponent keypad) {
    return Map.of(
        LastHourEntitySpawnStrategy.METADATA_TYPE,
        LastHourEntitySpawnStrategy.TYPE_KEYPAD,
        LastHourEntitySpawnStrategy.METADATA_KEYPAD_CORRECT_DIGITS,
        digitsToString(keypad.correctDigits()),
        LastHourEntitySpawnStrategy.METADATA_KEYPAD_ENTERED_DIGITS,
        digitsToString(keypad.enteredDigits()),
        LastHourEntitySpawnStrategy.METADATA_KEYPAD_UNLOCKED,
        String.valueOf(keypad.isUnlocked()),
        LastHourEntitySpawnStrategy.METADATA_KEYPAD_SHOW_DIGIT_COUNT,
        String.valueOf(keypad.showDigitCount()));
  }

  private Map<String, String> worldTimerMetadata(WorldTimerComponent worldTimer) {
    return Map.of(
        LastHourEntitySpawnStrategy.METADATA_TYPE,
        LastHourEntitySpawnStrategy.TYPE_WORLD_TIMER,
        LastHourEntitySpawnStrategy.METADATA_WORLD_TIMER_TIMESTAMP,
        String.valueOf(worldTimer.timestamp()),
        LastHourEntitySpawnStrategy.METADATA_WORLD_TIMER_DURATION,
        String.valueOf(worldTimer.duration()));
  }

  /**
   * Create a ComputerStateComponent from metadata if the type matches and the required fields are
   * present and valid.
   *
   * @param metadata the metadata to parse the ComputerStateComponent from
   * @return an Optional containing the ComputerStateComponent if parsing was successful, or an
   *     empty Optional if the type does not match or required fields are missing/invalid
   */
  public static Optional<ComputerStateComponent> computerStateFromMetadata(
      Map<String, String> metadata) {
    if (!LastHourEntitySpawnStrategy.TYPE_COMPUTER.equals(
            metadata.get(LastHourEntitySpawnStrategy.METADATA_TYPE))
        && !metadata.containsKey(LastHourEntitySpawnStrategy.METADATA_PROGRESS)) {
      return Optional.empty();
    }

    String progressRaw = metadata.get(LastHourEntitySpawnStrategy.METADATA_PROGRESS);
    if (progressRaw == null || progressRaw.isBlank()) {
      return Optional.empty();
    }

    ComputerProgress progress;
    try {
      progress = ComputerProgress.valueOf(progressRaw);
    } catch (IllegalArgumentException ex) {
      LOGGER.warn("Invalid computer progress metadata '{}'", progressRaw);
      return Optional.empty();
    }

    boolean infected =
        Boolean.parseBoolean(
            metadata.getOrDefault(LastHourEntitySpawnStrategy.METADATA_INFECTED, "false"));
    String virusType = metadata.getOrDefault(LastHourEntitySpawnStrategy.METADATA_VIRUS_TYPE, "");
    return Optional.of(new ComputerStateComponent(progress, infected, virusType));
  }

  /**
   * Create a KeypadComponent from metadata if the type matches and the required fields are present
   * and valid.
   *
   * @param metadata the metadata to parse the KeypadComponent from
   * @return an Optional containing the KeypadComponent if parsing was successful, or an empty
   *     Optional if the type does not match or required fields are missing/invalid
   */
  public static Optional<KeypadComponent> keypadStateFromMetadata(Map<String, String> metadata) {
    if (!LastHourEntitySpawnStrategy.TYPE_KEYPAD.equals(
        metadata.get(LastHourEntitySpawnStrategy.METADATA_TYPE))) {
      return Optional.empty();
    }

    String correctDigitsRaw =
        metadata.get(LastHourEntitySpawnStrategy.METADATA_KEYPAD_CORRECT_DIGITS);
    if (correctDigitsRaw == null) {
      return Optional.empty();
    }

    List<Integer> correctDigits = parseDigits(correctDigitsRaw);
    List<Integer> enteredDigits =
        parseDigits(
            metadata.getOrDefault(LastHourEntitySpawnStrategy.METADATA_KEYPAD_ENTERED_DIGITS, ""));
    boolean isUnlocked =
        Boolean.parseBoolean(
            metadata.getOrDefault(LastHourEntitySpawnStrategy.METADATA_KEYPAD_UNLOCKED, "false"));
    boolean showDigitCount =
        Boolean.parseBoolean(
            metadata.getOrDefault(
                LastHourEntitySpawnStrategy.METADATA_KEYPAD_SHOW_DIGIT_COUNT, "true"));

    return Optional.of(
        new KeypadComponent(correctDigits, enteredDigits, isUnlocked, showDigitCount));
  }

  /**
   * Create a WorldTimerComponent from metadata if the type matches and the required fields are
   * present and valid.
   *
   * @param metadata the metadata to parse the WorldTimerComponent from
   * @return an Optional containing the WorldTimerComponent if parsing was successful, or an empty
   *     Optional if the type does not match or required fields are missing/invalid
   */
  public static Optional<WorldTimerComponent> worldTimerStateFromMetadata(
      Map<String, String> metadata) {
    if (!LastHourEntitySpawnStrategy.TYPE_WORLD_TIMER.equals(
        metadata.get(LastHourEntitySpawnStrategy.METADATA_TYPE))) {
      return Optional.empty();
    }

    String timestampRaw = metadata.get(LastHourEntitySpawnStrategy.METADATA_WORLD_TIMER_TIMESTAMP);
    String durationRaw = metadata.get(LastHourEntitySpawnStrategy.METADATA_WORLD_TIMER_DURATION);
    if (timestampRaw == null || durationRaw == null) {
      return Optional.empty();
    }

    try {
      return Optional.of(
          new WorldTimerComponent(Integer.parseInt(timestampRaw), Integer.parseInt(durationRaw)));
    } catch (NumberFormatException ex) {
      LOGGER.warn(
          "Invalid world timer metadata timestamp='{}' duration='{}'", timestampRaw, durationRaw);
      return Optional.empty();
    }
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

  /**
   * Parses a comma-separated string of integers into a list of integers. If the input is null,
   * blank, or contains invalid integers, an empty list is returned.
   *
   * @param value the comma-separated string of integers to parse
   * @return a list of integers parsed from the input string, or an empty list if the input is null,
   *     blank, or contains invalid integers
   */
  public static List<Integer> parseDigits(String value) {
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
