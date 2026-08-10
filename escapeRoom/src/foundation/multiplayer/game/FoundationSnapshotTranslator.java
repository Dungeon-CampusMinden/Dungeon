package foundation.multiplayer.game;

import contrib.modules.keypad.KeypadComponent;
import core.Entity;
import core.Game;
import core.network.DefaultSnapshotTranslator;
import core.network.MessageDispatcher;
import core.network.SnapshotTranslator;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.SnapshotMessage;
import core.utils.logging.DungeonLogger;
import foundation.definition.NumericInputDefinition;
import foundation.room.model.FoundationRoom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adds the changing Foundation keypad state to Dungeon's ordinary world snapshots.
 *
 * <p>Every participant creates the static keypad configuration from its local complete DEER room.
 * Snapshots therefore synchronize the technical input identity, entered digits, and authoritative
 * unlocked state while the entity name remains player-facing.
 */
public final class FoundationSnapshotTranslator implements SnapshotTranslator {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(FoundationSnapshotTranslator.class);
  private static final String ENTERED_DIGITS = "foundation.keypad.enteredDigits";
  private static final String INPUT_ID = "foundation.keypad.inputId";
  private static final String UNLOCKED = "foundation.keypad.unlocked";

  private final SnapshotTranslator delegate = new DefaultSnapshotTranslator();
  private final Map<String, NumericInputDefinition> numericDefinitions;
  private final Map<Integer, String> numericEntityInputs = new HashMap<>();

  /**
   * Creates a translator backed by the participant-local complete room definition.
   *
   * @param room complete locally derived room
   */
  public FoundationSnapshotTranslator(final FoundationRoom room) {
    numericDefinitions =
        room.definition().sections().stream()
            .flatMap(section -> section.riddles().stream())
            .flatMap(riddle -> riddle.inputs().stream())
            .filter(NumericInputDefinition.class::isInstance)
            .map(NumericInputDefinition.class::cast)
            .collect(
                Collectors.toUnmodifiableMap(NumericInputDefinition::id, definition -> definition));
  }

  void registerKeypad(final int entityId, final String inputId) {
    if (!numericDefinitions.containsKey(inputId)) {
      throw new IllegalArgumentException("unknown numeric input definition: " + inputId);
    }
    String previous = numericEntityInputs.putIfAbsent(entityId, inputId);
    if (previous != null && !previous.equals(inputId)) {
      throw new IllegalArgumentException("keypad entity is already registered for another input");
    }
  }

  @Override
  public Optional<SnapshotMessage> translateToSnapshot(final int serverTick) {
    return delegate
        .translateToSnapshot(serverTick)
        .map(
            snapshot ->
                new SnapshotMessage(
                    snapshot.serverTick(),
                    snapshot.entities().stream().map(this::withKeypadState).toList(),
                    snapshot.levelState()));
  }

  @Override
  public void applySnapshot(final SnapshotMessage snapshot, final MessageDispatcher dispatcher) {
    delegate.applySnapshot(snapshot, dispatcher);
    snapshot.entities().forEach(this::applyKeypadState);
  }

  private EntityState withKeypadState(final EntityState state) {
    Optional<KeypadComponent> keypad =
        Game.findEntityById(state.entityId())
            .flatMap(entity -> entity.fetch(KeypadComponent.class));
    if (keypad.isEmpty()) {
      return state;
    }

    KeypadComponent component = keypad.orElseThrow();
    Map<String, String> metadata = new HashMap<>();
    state.metadata().ifPresent(metadata::putAll);
    String inputId = numericEntityInputs.get(state.entityId());
    if (inputId == null) {
      throw new IllegalStateException("Foundation keypad entity has no registered input identity");
    }
    metadata.put(ENTERED_DIGITS, digits(component.enteredDigits()));
    metadata.put(INPUT_ID, inputId);
    metadata.put(UNLOCKED, String.valueOf(component.isUnlocked()));

    EntityState.Builder builder = EntityState.builder().entityId(state.entityId());
    state.entityName().ifPresent(builder::entityName);
    state.position().ifPresent(builder::position);
    state.viewDirection().ifPresent(builder::viewDirection);
    state.rotation().ifPresent(builder::rotation);
    state.scale().ifPresent(builder::scale);
    state.currentHealth().ifPresent(builder::currentHealth);
    state.maxHealth().ifPresent(builder::maxHealth);
    state.currentMana().ifPresent(builder::currentMana);
    state.maxMana().ifPresent(builder::maxMana);
    state.stateName().ifPresent(builder::stateName);
    state.tintColor().ifPresent(builder::tintColor);
    state.inventory().ifPresent(builder::inventorySlots);
    return builder.metadata(metadata).build();
  }

  private void applyKeypadState(final EntityState state) {
    Optional<Map<String, String>> metadata = state.metadata();
    if (metadata.isEmpty() || !metadata.orElseThrow().containsKey(ENTERED_DIGITS)) {
      return;
    }
    Game.findEntityById(state.entityId())
        .ifPresent(entity -> applyKeypadState(entity, metadata.orElseThrow()));
  }

  private void applyKeypadState(final Entity entity, final Map<String, String> metadata) {
    try {
      List<Integer> enteredDigits = parseDigits(metadata.getOrDefault(ENTERED_DIGITS, ""));
      boolean unlocked = Boolean.parseBoolean(metadata.getOrDefault(UNLOCKED, "false"));
      String inputId = metadata.getOrDefault(INPUT_ID, "");
      NumericInputDefinition definition = numericDefinitions.get(inputId);
      if (definition == null) {
        throw new IllegalArgumentException(
            "keypad entity has no matching local numeric input definition");
      }

      KeypadComponent component =
          entity
              .fetch(KeypadComponent.class)
              .orElseGet(
                  () -> {
                    KeypadComponent created =
                        new KeypadComponent(
                            digits(definition.answer()), () -> {}, definition.showDigitCount());
                    entity.add(created);
                    return created;
                  });
      component.enteredDigits().clear();
      component.enteredDigits().addAll(enteredDigits);
      component.isUnlocked(unlocked);
    } catch (IllegalArgumentException exception) {
      LOGGER.warn(
          "Ignoring invalid Foundation keypad state for entity {}: {}",
          entity.id(),
          exception.getMessage());
    }
  }

  private static List<Integer> parseDigits(final String value) {
    if (value.length() > 8) {
      throw new IllegalArgumentException("too many entered digits");
    }
    List<Integer> digits = new ArrayList<>(value.length());
    for (int index = 0; index < value.length(); index++) {
      char digit = value.charAt(index);
      if (digit < '0' || digit > '9') {
        throw new IllegalArgumentException("entered digits contain a non-digit");
      }
      digits.add(digit - '0');
    }
    return digits;
  }

  private static String digits(final List<Integer> digits) {
    StringBuilder value = new StringBuilder(digits.size());
    digits.forEach(value::append);
    return value.toString();
  }

  private static ArrayList<Integer> digits(final String value) {
    ArrayList<Integer> digits = new ArrayList<>(value.length());
    value.chars().map(character -> character - '0').forEach(digits::add);
    return digits;
  }
}
