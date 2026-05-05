package network;

import contrib.modules.interaction.InteractionComponent;
import contrib.modules.keypad.KeypadComponent;
import contrib.modules.worldTimer.WorldTimerComponent;
import core.Entity;
import core.components.PositionComponent;
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
 * ComputerStateComponent}, {@link KeypadComponent}, {@link WorldTimerComponent}, and {@link
 * contrib.components.CollideComponent} entities.
 */
public final class LastHourEntitySpawnStrategy implements EntitySpawnStrategy {

  /** Metadata key identifying the custom entity type. */
  public static final String METADATA_TYPE = "lh.type";

  /** Type value for computer-state entities. */
  public static final String TYPE_COMPUTER = "computer-state";

  /** Type value for keypad entities. */
  public static final String TYPE_KEYPAD = "keypad";

  /** Type value for world-timer entities. */
  public static final String TYPE_WORLD_TIMER = "world-timer";

  /** Metadata key for the computer progress state. */
  public static final String METADATA_PROGRESS = "progress";

  /** Metadata key indicating whether the computer is infected. */
  public static final String METADATA_INFECTED = "isInfected";

  /** Metadata key for the virus type affecting the computer. */
  public static final String METADATA_VIRUS_TYPE = "virusType";

  /** Metadata key for the timestamp of login. */
  public static final String METADATA_TIMESTAMP_OF_LOGIN = "timestampOfLogin";

  /** Metadata key indicating whether the correct USB stick has been inserted. */
  public static final String METADATA_USB_INSERTED = "computer.usbInserted";

  /** Metadata key for the heater temperature. */
  public static final String METADATA_HEATER_CELSIUS = "computer.heaterCelsius";

  /** Metadata key indicating whether room lights are on. */
  public static final String METADATA_LIGHTS_ON = "computer.lightsOn";

  /** Metadata key indicating whether door 1 is open. */
  public static final String METADATA_DOOR1_OPEN = "computer.door1Open";

  /** Metadata key indicating whether door 2 has been unlocked. */
  public static final String METADATA_DOOR2_UNLOCKED = "computer.door2Unlocked";

  /** Metadata key indicating whether door 2 is open. */
  public static final String METADATA_DOOR2_OPEN = "computer.door2Open";

  /** Metadata key indicating whether the air conditioning is on. */
  public static final String METADATA_AC_ON = "computer.acOn";

  /** Metadata key indicating whether security cameras are on. */
  public static final String METADATA_CAMERAS_ON = "computer.camerasOn";

  /** Metadata key for the keypad's correct digit sequence. */
  public static final String METADATA_KEYPAD_CORRECT_DIGITS = "keypad.correctDigits";

  /** Metadata key for the digits entered on the keypad so far. */
  public static final String METADATA_KEYPAD_ENTERED_DIGITS = "keypad.enteredDigits";

  /** Metadata key indicating whether the keypad is unlocked. */
  public static final String METADATA_KEYPAD_UNLOCKED = "keypad.isUnlocked";

  /** Metadata key for the number of digits to display on the keypad. */
  public static final String METADATA_KEYPAD_SHOW_DIGIT_COUNT = "keypad.showDigitCount";

  /** Metadata key for the world timer's start timestamp. */
  public static final String METADATA_WORLD_TIMER_TIMESTAMP = "worldTimer.timestamp";

  /** Metadata key for the world timer's total duration. */
  public static final String METADATA_WORLD_TIMER_DURATION = "worldTimer.duration";

  /** Metadata key indicating whether the entity is interactable. */
  public static final String METADATA_INTERACTABLE = "interactable";

  private final EntitySpawnStrategy delegate = new DefaultEntitySpawnStrategy();

  /**
   * Builds a spawn event using default behavior first and falls back to metadata-only events for
   * custom entities.
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
        .fetch(ComputerStateComponent.class)
        .ifPresent(state -> metadata.putAll(computerStateMetadata(state)));
    entity
        .fetch(KeypadComponent.class)
        .ifPresent(keypad -> metadata.putAll(keypadMetadata(keypad)));
    entity
        .fetch(WorldTimerComponent.class)
        .ifPresent(worldTimer -> metadata.putAll(worldTimerMetadata(worldTimer)));
    entity
        .fetch(InteractionComponent.class)
        .ifPresent(interaction -> metadata.put(METADATA_INTERACTABLE, String.valueOf(true)));
    LastHourCollideSync.appendMetadata(entity, metadata);

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

    if (metadata.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        EntitySpawnEvent.builder()
            .entityId(entity.id())
            .positionComponent(entity.fetch(PositionComponent.class).orElse(null))
            .isPersistent(entity.isPersistent())
            .metadata(metadata)
            .build());
  }

  private Map<String, String> computerStateMetadata(ComputerStateComponent state) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put(METADATA_TYPE, TYPE_COMPUTER);
    metadata.put(METADATA_PROGRESS, state.state().name());
    metadata.put(METADATA_INFECTED, String.valueOf(state.isInfected()));
    metadata.put(METADATA_VIRUS_TYPE, state.virusType() == null ? "" : state.virusType());
    metadata.put(METADATA_TIMESTAMP_OF_LOGIN, String.valueOf(state.timestampOfLogin()));
    metadata.put(METADATA_USB_INSERTED, String.valueOf(state.usbInserted()));
    metadata.put(METADATA_LIGHTS_ON, String.valueOf(state.lightsOn()));
    metadata.put(METADATA_HEATER_CELSIUS, String.valueOf(state.heaterCelsius()));
    metadata.put(METADATA_DOOR1_OPEN, String.valueOf(state.door1Open()));
    metadata.put(METADATA_DOOR2_UNLOCKED, String.valueOf(state.door2Unlocked()));
    metadata.put(METADATA_DOOR2_OPEN, String.valueOf(state.door2Open()));
    metadata.put(METADATA_AC_ON, String.valueOf(state.acOn()));
    metadata.put(METADATA_CAMERAS_ON, String.valueOf(state.camerasOn()));
    return metadata;
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
