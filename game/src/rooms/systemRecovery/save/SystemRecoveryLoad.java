package rooms.systemRecovery.save;

import engine.Entity;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.utils.JsonHandler;
import engine.utils.Point;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.util.SystemRecoveryAchievementTracker;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.interpreter.TerminalStep;

/** Reads and applies a System Recovery checkpoint without replaying gameplay side effects. */
public final class SystemRecoveryLoad {

  private SystemRecoveryLoad() {}

  /**
   * Reads and validates a save file.
   *
   * @param path save file to read
   * @return validated data, or empty for a missing/invalid/unsupported file
   */
  public static Optional<SystemRecoverySave.SaveData> read(Path path) {
    if (path == null || !Files.isRegularFile(path)) return Optional.empty();
    try {
      return parse(Files.readString(path, StandardCharsets.UTF_8));
    } catch (IOException | RuntimeException ignored) {
      return Optional.empty();
    }
  }

  /**
   * Reads the default save selected by the main menu.
   *
   * @return validated default save, or empty when none is available
   */
  public static Optional<SystemRecoverySave.SaveData> read() {
    return read(SystemRecoverySave.DEFAULT_PATH);
  }

  /**
   * Resolves the checkpoint key without changing the running game.
   *
   * @param data parsed save data
   * @return recognized main-riddle checkpoint, or empty for invalid data
   */
  public static Optional<SystemRecoveryLearningStep> checkpoint(SystemRecoverySave.SaveData data) {
    return data == null ? Optional.empty() : findCheckpoint(data.checkpointKey());
  }

  /**
   * Restores the Petri marking, terminal capture context and quest log silently.
   *
   * <p>World objects are restored by the level's checkpoint projection after all entities have been
   * set up. This method only restores the state that has no visual ownership in a riddle.
   *
   * @param data validated save data
   * @return the restored checkpoint
   */
  public static Optional<SystemRecoveryLearningStep> restoreRuntime(
      SystemRecoverySave.SaveData data) {
    if (data == null) return Optional.empty();
    SystemRecoveryLearningStep checkpoint = findCheckpoint(data.checkpointKey()).orElse(null);
    if (checkpoint == null) return Optional.empty();
    if (!hasExpectedHistory(checkpoint, data.acceptedTerminalInputs())) return Optional.empty();
    try {
      List<TerminalInterpreter.AcceptedInput> inputs =
          data.acceptedTerminalInputs().stream()
              .map(input -> new TerminalInterpreter.AcceptedInput(input.state(), input.source()))
              .toList();
      TerminalInterpreter.instance().restoreAcceptedInputs(inputs, checkpoint.terminalState());
      if (!SystemRecoveryProgressNet.restoreActiveStep(checkpoint)) return Optional.empty();
      restoreQuestLog(data.questLog());
      if (data.achievementProgress() != null) {
        SystemRecoveryAchievements.restore(data.achievementProgress());
      }
      return Optional.of(checkpoint);
    } catch (RuntimeException ignored) {
      return Optional.empty();
    }
  }

  /**
   * Applies each saved position to its named player, preserving positions until players join.
   *
   * @param players currently connected player entities
   * @param pendingPositions saved positions awaiting restoration
   * @return positions whose player has not joined yet
   */
  public static List<SystemRecoverySave.PlayerPositionData> restorePlayerPositions(
      List<Entity> players, List<SystemRecoverySave.PlayerPositionData> pendingPositions) {
    Set<String> restoredNames = new HashSet<>();
    for (SystemRecoverySave.PlayerPositionData saved : pendingPositions) {
      players.stream()
          .filter(
              player ->
                  player
                      .fetch(PlayerComponent.class)
                      .map(PlayerComponent::playerName)
                      .filter(saved.playerName()::equals)
                      .isPresent())
          .findFirst()
          .flatMap(player -> player.fetch(PositionComponent.class))
          .ifPresent(
              position -> {
                position.position(new Point(saved.x(), saved.y()));
                restoredNames.add(saved.playerName());
              });
    }
    return pendingPositions.stream()
        .filter(position -> !restoredNames.contains(position.playerName()))
        .toList();
  }

  private static void restoreQuestLog(List<SystemRecoverySave.QuestLogEntryData> entries) {
    QuestLogUtil.getQuestLogComponent()
        .ifPresent(
            component -> {
              for (SystemRecoverySave.QuestLogEntryData entry : entries) {
                if (entry.tab() == null || entry.text() == null) continue;
                component.add(
                    entry.tab(),
                    new QuestLogEntry(
                        entry.text(),
                        entry.timestamp(),
                        entry.userCreated(),
                        entry.owner(),
                        entry.onlyForCreator()));
              }
            });
  }

  private static Optional<SystemRecoverySave.SaveData> parse(String json) {
    Map<String, Object> root = JsonHandler.readJson(json);
    int version = integer(root.get("formatVersion"));
    if (version != SystemRecoverySave.FORMAT_VERSION) return Optional.empty();
    String checkpoint = string(root.get("checkpoint"));
    if (checkpoint == null || findCheckpoint(checkpoint).isEmpty()) return Optional.empty();
    Map<?, ?> metadata = metadataMap(root.get("metadata"));
    UUID runId =
        optionalUuid(root.get("runId"))
            .or(() -> optionalUuid(metadata.get("runId")))
            .orElseGet(UUID::randomUUID);
    String playerName = string(root.get("playerName"));
    if (playerName == null) playerName = string(metadata.get("playerName"));
    Boolean trackingConsent = booleanOrNull(root.get("trackingConsent"));
    if (trackingConsent == null) trackingConsent = booleanOrNull(metadata.get("trackingConsent"));

    List<SystemRecoverySave.AcceptedInput> inputs = new ArrayList<>();
    for (Object value : list(root.get("acceptedTerminalInputs"))) {
      if (!(value instanceof Map<?, ?> map)) return Optional.empty();
      inputs.add(
          new SystemRecoverySave.AcceptedInput(
              integer(map.get("state")), stringRequired(map.get("source"))));
    }

    List<SystemRecoverySave.QuestLogEntryData> questLog = new ArrayList<>();
    for (Object value : list(root.get("questLog"))) {
      if (!(value instanceof Map<?, ?> map)) return Optional.empty();
      String tab = stringRequired(map.get("tab"));
      String text = stringRequired(map.get("text"));
      int timestamp = integer(map.get("timestamp"));
      boolean userCreated = booleanValue(map.get("userCreated"));
      String owner = stringRequired(map.get("owner"));
      boolean onlyForCreator = booleanValue(map.get("onlyForCreator"));
      questLog.add(
          new SystemRecoverySave.QuestLogEntryData(
              tab, text, timestamp, userCreated, owner, onlyForCreator));
    }
    if (!hasExpectedHistory(findCheckpoint(checkpoint).orElseThrow(), inputs)) {
      return Optional.empty();
    }
    List<String> terminalHistory = strings(root.get("terminalHistory"));
    List<String> memoryWatchEntries = strings(root.get("memoryWatch"));
    List<SystemRecoverySave.PlayerItemData> inventoryItems = new ArrayList<>();
    for (Object value : list(root.get("inventoryItems"))) {
      if (!(value instanceof Map<?, ?> map)) return Optional.empty();
      inventoryItems.add(
          new SystemRecoverySave.PlayerItemData(
              string(map.get("playerName")),
              stringRequired(map.get("itemKey")),
              booleanValue(map.get("programmed")),
              string(map.get("draft"))));
    }
    List<SystemRecoverySave.PlayerPositionData> playerPositions = new ArrayList<>();
    for (Object value : list(root.get("players"))) {
      if (!(value instanceof Map<?, ?> map)) return Optional.empty();
      playerPositions.add(
          new SystemRecoverySave.PlayerPositionData(
              stringRequired(map.get("playerName")),
              floatValue(map.get("x")),
              floatValue(map.get("y"))));
    }
    if (!hasRequiredCheckpointItem(findCheckpoint(checkpoint).orElseThrow(), inventoryItems)) {
      return Optional.empty();
    }
    boolean systemCoreExitOpen =
        root.containsKey("systemCoreExitOpen") && booleanValue(root.get("systemCoreExitOpen"));
    boolean systemCoreWarningCallAnswered =
        root.containsKey("systemCoreWarningCallAnswered")
            && booleanValue(root.get("systemCoreWarningCallAnswered"));
    SystemRecoveryAchievementTracker.Snapshot achievementProgress =
        version >= 2 && root.containsKey("achievementProgress")
            ? parseAchievementProgress(root.get("achievementProgress"))
            : null;
    return Optional.of(
        new SystemRecoverySave.SaveData(
            checkpoint,
            inputs,
            questLog,
            runId,
            playerName,
            trackingConsent,
            achievementProgress,
            terminalHistory,
            memoryWatchEntries,
            inventoryItems,
            systemCoreExitOpen,
            systemCoreWarningCallAnswered,
            playerPositions,
            ((Number) root.get("activeMs")).longValue()));
  }

  private static SystemRecoveryAchievementTracker.Snapshot parseAchievementProgress(Object value) {
    if (!(value instanceof Map<?, ?> map)) {
      throw new IllegalArgumentException("Expected achievement progress object");
    }
    return new SystemRecoveryAchievementTracker.Snapshot(
        booleanValue(map.get("debugRun")),
        booleanValue(map.get("firstTerminalAttemptSeen")),
        optionalBooleanValue(map.get("phoneAnsweredBeforeMainQuestCall")),
        integer(map.get("wrongTerminalAttempts")),
        integer(map.get("acceptedHints")),
        strings(map.get("hintedPuzzles")),
        strings(map.get("solvedPuzzles")),
        strings(map.get("failedPuzzles")),
        strings(map.get("failedTerminalPuzzles")),
        strings(map.get("failedUploads")),
        strings(map.get("acceptedUploads")),
        strings(map.get("emittedAchievements")));
  }

  private static List<String> strings(Object value) {
    return list(value).stream().map(SystemRecoveryLoad::stringRequired).toList();
  }

  private static boolean hasExpectedHistory(
      SystemRecoveryLearningStep checkpoint, List<SystemRecoverySave.AcceptedInput> inputs) {
    int expectedInputCount = checkpoint.acceptedTerminalInputCount();
    int targetState = checkpoint.terminalState();
    if (expectedInputCount < 0
        || targetState < 0
        || inputs == null
        || inputs.size() != expectedInputCount) {
      return false;
    }
    int expectedState = 0;
    for (SystemRecoverySave.AcceptedInput input : inputs) {
      if (input == null || input.source() == null) return false;
      expectedState = skipNonTerminalStates(expectedState, input.state());
      TerminalStep inputStep = TerminalStep.fromStateId(input.state()).orElse(null);
      if (input.state() != expectedState
          || inputStep == null
          || inputStep.inputMode() != TerminalStep.InputMode.TERMINAL) return false;
      expectedState++;
    }
    return skipNonTerminalStates(expectedState, targetState) == targetState;
  }

  private static int skipNonTerminalStates(int currentState, int targetState) {
    int state = currentState;
    while (state < targetState) {
      TerminalStep step = TerminalStep.fromStateId(state).orElse(null);
      if (step == null || step.inputMode() == TerminalStep.InputMode.TERMINAL) break;
      state++;
    }
    return state;
  }

  private static boolean hasRequiredCheckpointItem(
      SystemRecoveryLearningStep checkpoint, List<SystemRecoverySave.PlayerItemData> items) {
    String requiredItem =
        switch (checkpoint) {
          case BUBBLE_SORT_MACHINE -> "sort-program-stick";
          case SEARCH_ROBOT_RUN -> "search-program-chip";
          default -> null;
        };
    return requiredItem == null
        || items.stream()
            .anyMatch(item -> requiredItem.equals(item.itemKey()) && item.programmed());
  }

  private static Optional<SystemRecoveryLearningStep> findCheckpoint(String key) {
    return java.util.Arrays.stream(SystemRecoveryLearningStep.values())
        .filter(step -> step.hintKey().equals(key))
        .filter(SystemRecoveryLoad::isMainPuzzleCheckpoint)
        .findFirst();
  }

  /**
   * Returns whether a step is a supported save checkpoint.
   *
   * @param step learning step to classify
   * @return whether the step is a supported checkpoint
   */
  public static boolean isMainPuzzleCheckpoint(SystemRecoveryLearningStep step) {
    return switch (step) {
      case ENERGY_ARRAY,
          MODULE_ARRAY,
          INVENTORY_COUNT,
          TRANSPORT_ARRAY,
          MANUAL_SORTING,
          BUBBLE_SORT_CONDITION,
          BUBBLE_SORT_MACHINE,
          ARCHIVE_ACCESS,
          ARCHIVE_ARRAYS,
          STORAGE_ARRAY,
          SEARCH_PROGRAM,
          SEARCH_ROBOT_RUN,
          SYSTEM_CORE_ACCESS,
          CORE_SORT,
          CORE_COUNT,
          CORE_SEARCH,
          CORE_SEARCH_ROBOT,
          CORE_META,
          COMPLETE ->
          true;
      default -> false;
    };
  }

  /**
   * Returns whether a newly reached checkpoint may be written by the automatic save system.
   *
   * <p>The initial energy-array step remains a valid legacy/load checkpoint, but it is deliberately
   * excluded here. The first new save is created at {@code MODULE_ARRAY}, after the player has
   * completed the first riddle and inserted the battery. Later puzzle places include successful
   * chip uploads, the consumed-key archive door transition, every System Core substep and final
   * completion.
   *
   * @param step learning step to classify
   * @return whether the step may trigger an automatic save
   */
  public static boolean isAutoSaveCheckpoint(SystemRecoveryLearningStep step) {
    return step != null
        && step != SystemRecoveryLearningStep.ENERGY_ARRAY
        && isMainPuzzleCheckpoint(step);
  }

  private static List<?> list(Object value) {
    if (value == null) return List.of();
    if (!(value instanceof List<?> list)) throw new IllegalArgumentException("Expected JSON array");
    return list;
  }

  private static int integer(Object value) {
    if (!(value instanceof Number number)) throw new IllegalArgumentException("Expected number");
    return number.intValue();
  }

  private static float floatValue(Object value) {
    if (!(value instanceof Number number)) throw new IllegalArgumentException("Expected number");
    return number.floatValue();
  }

  private static boolean booleanValue(Object value) {
    if (!(value instanceof Boolean booleanValue)) {
      throw new IllegalArgumentException("Expected boolean");
    }
    return booleanValue;
  }

  private static boolean optionalBooleanValue(Object value) {
    return value == null ? false : booleanValue(value);
  }

  private static Boolean booleanOrNull(Object value) {
    if (value == null) return null;
    return booleanValue(value);
  }

  private static String string(Object value) {
    return value instanceof String text ? text : null;
  }

  private static String stringRequired(Object value) {
    String text = string(value);
    if (text == null) throw new IllegalArgumentException("Expected string");
    return text;
  }

  private static Optional<UUID> optionalUuid(Object value) {
    if (value == null) return Optional.empty();
    if (!(value instanceof String text)) {
      throw new IllegalArgumentException("Expected UUID string");
    }
    return Optional.of(UUID.fromString(text));
  }

  private static Map<?, ?> metadataMap(Object value) {
    if (value == null) return Map.of();
    if (!(value instanceof Map<?, ?> metadata)) {
      throw new IllegalArgumentException("Expected save metadata object");
    }
    return metadata;
  }
}
