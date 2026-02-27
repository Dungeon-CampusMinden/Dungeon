package network;

import contrib.modules.keypad.KeypadComponent;
import contrib.modules.worldTimer.WorldTimerComponent;
import core.Entity;
import core.network.config.DefaultEntitySpawnStrategy;
import core.network.config.EntitySpawnStrategy;
import core.network.messages.s2c.EntitySpawnEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import modules.computer.ComputerStateComponent;

/**
 * Entity spawn strategy for The Last Hour that supports metadata-only spawn events for {@link
 * ComputerStateComponent} entities.
 */
public final class LastHourEntitySpawnStrategy implements EntitySpawnStrategy {

  private static final String METADATA_TYPE = "lh.type";
  private static final String TYPE_COMPUTER = "computer-state";
  private static final String TYPE_KEYPAD = "keypad";
  private static final String TYPE_WORLD_TIMER = "world-timer";
  private static final String METADATA_PROGRESS = "progress";
  private static final String METADATA_INFECTED = "isInfected";
  private static final String METADATA_VIRUS_TYPE = "virusType";
  private static final String METADATA_KEYPAD_CORRECT_DIGITS = "keypad.correctDigits";
  private static final String METADATA_KEYPAD_ENTERED_DIGITS = "keypad.enteredDigits";
  private static final String METADATA_KEYPAD_UNLOCKED = "keypad.isUnlocked";
  private static final String METADATA_KEYPAD_SHOW_DIGIT_COUNT = "keypad.showDigitCount";
  private static final String METADATA_WORLD_TIMER_TIMESTAMP = "worldTimer.timestamp";
  private static final String METADATA_WORLD_TIMER_DURATION = "worldTimer.duration";

  private final EntitySpawnStrategy delegate = new DefaultEntitySpawnStrategy();

  /**
   * Builds a spawn event using default behavior first and falls back to metadata-only events for
   * computer-state entities.
   *
   * @param entity the source entity
   * @return an Optional containing a spawn event if the entity is spawnable, otherwise empty
   */
  @Override
  public Optional<EntitySpawnEvent> buildSpawnEvent(Entity entity) {
    Optional<EntitySpawnEvent> defaultSpawn = delegate.buildSpawnEvent(entity);
    Map<String, String> metadata = new HashMap<>();
    defaultSpawn.ifPresent(spawnEvent -> metadata.putAll(spawnEvent.metadata()));

    entity
        .fetch(KeypadComponent.class)
        .ifPresent(keypad -> metadata.putAll(keypadMetadata(keypad)));
    entity
        .fetch(WorldTimerComponent.class)
        .ifPresent(worldTimer -> metadata.putAll(worldTimerMetadata(worldTimer)));

    if (defaultSpawn.isPresent() && !metadata.isEmpty()) {
      EntitySpawnEvent base = defaultSpawn.orElseThrow();
      return Optional.of(
          EntitySpawnEvent.builder()
              .entityId(base.entityId())
              .positionComponent(base.positionComponent())
              .drawInfo(base.drawInfo())
              .isPersistent(base.isPersistent())
              .playerComponent(base.playerComponent())
              .characterClassId(base.characterClassId())
              .metadata(metadata)
              .build());
    }

    if (defaultSpawn.isPresent()) {
      return defaultSpawn;
    }

    entity
        .fetch(WorldTimerComponent.class)
        .ifPresent(worldTimer -> metadata.putAll(worldTimerMetadata(worldTimer)));

    return entity
        .fetch(ComputerStateComponent.class)
        .map(
            state -> {
              Map<String, String> mergedMetadata = new HashMap<>(metadata);
              mergedMetadata.putAll(computerStateMetadata(state));
              return EntitySpawnEvent.builder()
                  .entityId(entity.id())
                  .isPersistent(false)
                  .metadata(mergedMetadata)
                  .build();
            })
        .or(
            () ->
                metadata.isEmpty()
                    ? Optional.empty()
                    : Optional.of(
                        EntitySpawnEvent.builder()
                            .entityId(entity.id())
                            .isPersistent(false)
                            .metadata(metadata)
                            .build()));
  }

  private Map<String, String> computerStateMetadata(ComputerStateComponent state) {
    return Map.of(
        METADATA_TYPE,
        TYPE_COMPUTER,
        METADATA_PROGRESS,
        state.state().name(),
        METADATA_INFECTED,
        String.valueOf(state.isInfected()),
        METADATA_VIRUS_TYPE,
        state.virusType() == null ? "" : state.virusType());
  }

  private Map<String, String> keypadMetadata(KeypadComponent keypad) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put(METADATA_TYPE, TYPE_KEYPAD);
    metadata.put(METADATA_KEYPAD_CORRECT_DIGITS, digitsToString(keypad.correctDigits()));
    metadata.put(METADATA_KEYPAD_ENTERED_DIGITS, digitsToString(keypad.enteredDigits()));
    metadata.put(METADATA_KEYPAD_UNLOCKED, String.valueOf(keypad.isUnlocked()));
    metadata.put(METADATA_KEYPAD_SHOW_DIGIT_COUNT, String.valueOf(keypad.showDigitCount()));
    return metadata;
  }

  private Map<String, String> worldTimerMetadata(WorldTimerComponent worldTimer) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put(METADATA_TYPE, TYPE_WORLD_TIMER);
    metadata.put(METADATA_WORLD_TIMER_TIMESTAMP, String.valueOf(worldTimer.timestamp()));
    metadata.put(METADATA_WORLD_TIMER_DURATION, String.valueOf(worldTimer.duration()));
    return metadata;
  }

  private String digitsToString(List<Integer> digits) {
    return digits.stream()
        .map(String::valueOf)
        .reduce((left, right) -> left + "," + right)
        .orElse("");
  }
}
