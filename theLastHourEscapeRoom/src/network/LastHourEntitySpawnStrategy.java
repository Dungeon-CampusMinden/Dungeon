package network;

import contrib.modules.keypad.KeypadComponent;
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
  private static final String METADATA_PROGRESS = "progress";
  private static final String METADATA_INFECTED = "isInfected";
  private static final String METADATA_VIRUS_TYPE = "virusType";
  private static final String METADATA_KEYPAD_CORRECT_DIGITS = "keypad.correctDigits";
  private static final String METADATA_KEYPAD_ENTERED_DIGITS = "keypad.enteredDigits";
  private static final String METADATA_KEYPAD_UNLOCKED = "keypad.isUnlocked";
  private static final String METADATA_KEYPAD_SHOW_DIGIT_COUNT = "keypad.showDigitCount";

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

    if (defaultSpawn.isPresent() && entity.fetch(KeypadComponent.class).isPresent()) {
      KeypadComponent keypad = entity.fetch(KeypadComponent.class).orElseThrow();
      Map<String, String> metadata = new HashMap<>(defaultSpawn.orElseThrow().metadata());
      metadata.put(METADATA_TYPE, TYPE_KEYPAD);
      metadata.put(METADATA_KEYPAD_CORRECT_DIGITS, digitsToString(keypad.correctDigits()));
      metadata.put(METADATA_KEYPAD_ENTERED_DIGITS, digitsToString(keypad.enteredDigits()));
      metadata.put(METADATA_KEYPAD_UNLOCKED, String.valueOf(keypad.isUnlocked()));
      metadata.put(METADATA_KEYPAD_SHOW_DIGIT_COUNT, String.valueOf(keypad.showDigitCount()));
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

    return entity
        .fetch(ComputerStateComponent.class)
        .map(
            state ->
                EntitySpawnEvent.builder()
                    .entityId(entity.id())
                    .isPersistent(false)
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
                    .build());
  }

  private String digitsToString(List<Integer> digits) {
    return digits.stream()
        .map(String::valueOf)
        .reduce((left, right) -> left + "," + right)
        .orElse("");
  }
}
