package rooms.systemRecovery.network;

import engine.Entity;
import engine.components.PositionComponent;
import engine.network.config.DefaultEntitySpawnStrategy;
import engine.network.config.EntitySpawnStrategy;
import engine.network.messages.s2c.EntitySpawnEvent;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Entity spawn strategy for System Recovery metadata. */
public final class SystemRecoveryEntitySpawnStrategy implements EntitySpawnStrategy {

  /** Metadata key identifying the custom entity type. */
  public static final String METADATA_TYPE = "systemRecovery.type";

  /** Type value for keypad entities. */
  public static final String TYPE_KEYPAD = "keypad";

  /** Metadata key indicating whether the entity is interactable. */
  public static final String METADATA_INTERACTABLE = "systemRecovery.interactable";

  /** Metadata key for the keypad's correct digit sequence. */
  public static final String METADATA_KEYPAD_CORRECT_DIGITS =
      "systemRecovery.keypad.correctDigits";

  /** Metadata key for the digits entered on the keypad so far. */
  public static final String METADATA_KEYPAD_ENTERED_DIGITS =
      "systemRecovery.keypad.enteredDigits";

  /** Metadata key indicating whether the keypad is unlocked. */
  public static final String METADATA_KEYPAD_UNLOCKED = "systemRecovery.keypad.isUnlocked";

  /** Metadata key for the number of digits to display on the keypad. */
  public static final String METADATA_KEYPAD_SHOW_DIGIT_COUNT =
      "systemRecovery.keypad.showDigitCount";

  private final EntitySpawnStrategy delegate = new DefaultEntitySpawnStrategy();

  /**
   * Builds a spawn event using default behavior and appends basic-room metadata where needed.
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
        .fetch(InteractionComponent.class)
        .ifPresent(interaction -> metadata.put(METADATA_INTERACTABLE, String.valueOf(true)));
    SystemRecoveryCollideSync.appendMetadata(entity, metadata);

    if (defaultSpawn.isPresent() && !metadata.isEmpty()) {
      EntitySpawnEvent base = defaultSpawn.orElseThrow();
      return Optional.of(
          EntitySpawnEvent.builder()
              .entityId(base.entityId())
              .positionComponent(base.positionComponent())
              .drawInfo(base.drawInfo())
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
            .metadata(metadata)
            .build());
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

  private String digitsToString(List<Integer> digits) {
    return digits.stream()
        .map(String::valueOf)
        .reduce((left, right) -> left + "," + right)
        .orElse("");
  }
}
