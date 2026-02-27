package network;

import contrib.modules.keypad.KeypadComponent;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import modules.computer.ComputerProgress;
import modules.computer.ComputerStateComponent;

/**
 * Snapshot translator for The Last Hour that synchronizes the shared {@link ComputerStateComponent}
 * through metadata.
 */
public final class LastHourSnapshotTranslator implements SnapshotTranslator {

  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(LastHourSnapshotTranslator.class);
  private static final String METADATA_TYPE = "lh.type";
  private static final String TYPE_COMPUTER = "computer-state";
  private static final String TYPE_KEYPAD = "keypad";
  private static final String METADATA_PROGRESS = "progress";
  private static final String METADATA_INFECTED = "isInfected";
  private static final String METADATA_VIRUS_TYPE = "virusType";
  private static final String METADATA_KEYPAD_CORRECT_DIGITS = "keypad.correctDigits";
  private static final String METADATA_KEYPAD_ENTERED_DIGITS = "keypad.enteredDigits";
  private static final String METADATA_KEYPAD_UNLOCKED = "keypad.isUnlocked";
  private static final String METADATA_KEYPAD_SHOW_DIGIT_COUNT = "keypad.showDigitCount";

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
                entities.set(index, withMetadata(baseState, keypadMetadata(keypad)));
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
      Optional<KeypadState> mappedKeypadState =
          entityState.metadata().flatMap(this::keypadStateFromMetadata);
      if (mappedKeypadState.isPresent()) {
        KeypadState keypadState = mappedKeypadState.orElseThrow();
        Game.findEntityById(entityState.entityId())
            .ifPresent(entity -> applyKeypadState(entity, keypadState));
        continue;
      }

      Optional<ComputerStateComponent> mappedState =
          entityState.metadata().flatMap(this::computerStateFromMetadata);
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
                METADATA_TYPE,
                TYPE_COMPUTER,
                METADATA_PROGRESS,
                state.state().name(),
                METADATA_INFECTED,
                String.valueOf(state.isInfected()),
                METADATA_VIRUS_TYPE,
                state.virusType() == null ? "" : state.virusType()))
        .build();
  }

  private EntityState withMetadata(EntityState baseState, Map<String, String> metadata) {
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
    builder.metadata(metadata);
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
        METADATA_TYPE,
        TYPE_KEYPAD,
        METADATA_KEYPAD_CORRECT_DIGITS,
        digitsToString(keypad.correctDigits()),
        METADATA_KEYPAD_ENTERED_DIGITS,
        digitsToString(keypad.enteredDigits()),
        METADATA_KEYPAD_UNLOCKED,
        String.valueOf(keypad.isUnlocked()),
        METADATA_KEYPAD_SHOW_DIGIT_COUNT,
        String.valueOf(keypad.showDigitCount()));
  }

  private Optional<ComputerStateComponent> computerStateFromMetadata(Map<String, String> metadata) {
    if (!TYPE_COMPUTER.equals(metadata.get(METADATA_TYPE))
        && !metadata.containsKey(METADATA_PROGRESS)) {
      return Optional.empty();
    }

    String progressRaw = metadata.get(METADATA_PROGRESS);
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

    boolean infected = Boolean.parseBoolean(metadata.getOrDefault(METADATA_INFECTED, "false"));
    String virusType = metadata.getOrDefault(METADATA_VIRUS_TYPE, "");
    return Optional.of(new ComputerStateComponent(progress, infected, virusType));
  }

  private Optional<KeypadState> keypadStateFromMetadata(Map<String, String> metadata) {
    if (!TYPE_KEYPAD.equals(metadata.get(METADATA_TYPE))) {
      return Optional.empty();
    }

    String correctDigitsRaw = metadata.get(METADATA_KEYPAD_CORRECT_DIGITS);
    if (correctDigitsRaw == null) {
      return Optional.empty();
    }

    List<Integer> correctDigits = parseDigits(correctDigitsRaw);
    List<Integer> enteredDigits =
        parseDigits(metadata.getOrDefault(METADATA_KEYPAD_ENTERED_DIGITS, ""));
    boolean isUnlocked =
        Boolean.parseBoolean(metadata.getOrDefault(METADATA_KEYPAD_UNLOCKED, "false"));
    boolean showDigitCount =
        Boolean.parseBoolean(metadata.getOrDefault(METADATA_KEYPAD_SHOW_DIGIT_COUNT, "true"));

    return Optional.of(new KeypadState(correctDigits, enteredDigits, isUnlocked, showDigitCount));
  }

  private void applyKeypadState(Entity entity, KeypadState keypadState) {
    KeypadComponent component =
        entity
            .fetch(KeypadComponent.class)
            .orElseGet(
                () -> {
                  KeypadComponent newComponent =
                      new KeypadComponent(
                          keypadState.correctDigits(), () -> {}, keypadState.showDigitCount());
                  entity.add(newComponent);
                  return newComponent;
                });
    component.enteredDigits().clear();
    component.enteredDigits().addAll(keypadState.enteredDigits());
    component.isUnlocked(keypadState.isUnlocked());
    component.showDigitCount(keypadState.showDigitCount());
  }

  private List<Integer> parseDigits(String value) {
    if (value == null || value.isBlank()) {
      return new ArrayList<>();
    }
    try {
      return List.of(value.split(",")).stream()
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

  private record KeypadState(
      List<Integer> correctDigits,
      List<Integer> enteredDigits,
      boolean isUnlocked,
      boolean showDigitCount) {}
}
