package rooms.programming.network;

import engine.utils.logging.DungeonLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import rooms.programming.state.GolemState;
import rooms.programming.state.ProgrammingPhase;
import rooms.programming.state.ProgrammingStateComponent;

/** Metadata codec for the shared Programming 1 room state. */
final class ProgrammingStateMetadata {

  static final String TYPE_KEY = "programming.type";
  static final String ROOM_STATE_TYPE = "room-state";

  private static final String PHASE_KEY = "programming.phase";
  private static final String GOLEM_PRESENT_KEY = "programming.golem.present";
  private static final String GOLEM_NAME_KEY = "programming.golem.name";
  private static final String GOLEM_LIFE_ENERGY_KEY = "programming.golem.lifeEnergy";
  private static final String GOLEM_MANA_KEY = "programming.golem.mana";
  private static final String GOLEM_ACTIVATED_KEY = "programming.golem.activated";
  private static final String GOLEM_VIEW_DIRECTION_KEY = "programming.golem.viewDirection";
  private static final String GOLEM_STEPS_KEY = "programming.golem.steps";

  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(ProgrammingStateMetadata.class);

  private ProgrammingStateMetadata() {}

  static Map<String, String> encode(ProgrammingStateComponent state) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put(TYPE_KEY, ROOM_STATE_TYPE);
    metadata.put(PHASE_KEY, state.phase().name());
    metadata.put(GOLEM_PRESENT_KEY, String.valueOf(state.golem().isPresent()));
    state
        .golem()
        .ifPresent(
            golem -> {
              metadata.put(GOLEM_NAME_KEY, golem.name());
              metadata.put(GOLEM_LIFE_ENERGY_KEY, String.valueOf(golem.lifeEnergy()));
              metadata.put(GOLEM_MANA_KEY, String.valueOf(golem.mana()));
              metadata.put(GOLEM_ACTIVATED_KEY, String.valueOf(golem.activated()));
              metadata.put(GOLEM_VIEW_DIRECTION_KEY, String.valueOf(golem.viewDirection()));
              metadata.put(GOLEM_STEPS_KEY, String.valueOf(golem.steps()));
            });
    return Map.copyOf(metadata);
  }

  static Optional<ProgrammingStateComponent> decode(Map<String, String> metadata) {
    if (!ROOM_STATE_TYPE.equals(metadata.get(TYPE_KEY))) {
      return Optional.empty();
    }

    try {
      ProgrammingPhase phase = ProgrammingPhase.valueOf(required(metadata, PHASE_KEY));
      boolean golemPresent = parseBoolean(required(metadata, GOLEM_PRESENT_KEY));
      if (!golemPresent) {
        return Optional.of(new ProgrammingStateComponent(phase, Optional.empty()));
      }

      String direction = required(metadata, GOLEM_VIEW_DIRECTION_KEY);
      if (direction.length() != 1) {
        throw new IllegalArgumentException("view direction must contain one character");
      }
      GolemState golem =
          new GolemState(
              required(metadata, GOLEM_NAME_KEY),
              Integer.parseInt(required(metadata, GOLEM_LIFE_ENERGY_KEY)),
              Double.parseDouble(required(metadata, GOLEM_MANA_KEY)),
              parseBoolean(required(metadata, GOLEM_ACTIVATED_KEY)),
              direction.charAt(0),
              Integer.parseInt(required(metadata, GOLEM_STEPS_KEY)));
      return Optional.of(new ProgrammingStateComponent(phase, Optional.of(golem)));
    } catch (IllegalArgumentException exception) {
      LOGGER.warn("Ignoring invalid Programming room-state metadata: {}", exception.getMessage());
      return Optional.empty();
    }
  }

  private static String required(Map<String, String> metadata, String key) {
    String value = metadata.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("missing metadata key " + key);
    }
    return value;
  }

  private static boolean parseBoolean(String value) {
    return switch (value) {
      case "true" -> true;
      case "false" -> false;
      default -> throw new IllegalArgumentException("invalid boolean " + value);
    };
  }
}
