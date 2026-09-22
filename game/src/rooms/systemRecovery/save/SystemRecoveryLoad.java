package rooms.systemRecovery.save;

import engine.utils.JsonHandler;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.util.SystemRecoveryAchievementTracker;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;

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
      TerminalInterpreter.instance().restoreAcceptedInputs(inputs);
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
    if (version < 1 || version > SystemRecoverySave.FORMAT_VERSION) return Optional.empty();
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
              tab,
              text,
              timestamp,
              userCreated,
              owner,
              onlyForCreator));
    }
    if (!hasExpectedHistory(findCheckpoint(checkpoint).orElseThrow(), inputs)) {
      return Optional.empty();
    }
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
            achievementProgress));
  }

  private static SystemRecoveryAchievementTracker.Snapshot parseAchievementProgress(Object value) {
    if (!(value instanceof Map<?, ?> map)) {
      throw new IllegalArgumentException("Expected achievement progress object");
    }
    return new SystemRecoveryAchievementTracker.Snapshot(
        booleanValue(map.get("debugRun")),
        booleanValue(map.get("firstTerminalAttemptSeen")),
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
    if (expectedInputCount < 0 || inputs == null || inputs.size() != expectedInputCount)
      return false;
    for (int index = 0; index < inputs.size(); index++) {
      SystemRecoverySave.AcceptedInput input = inputs.get(index);
      if (input == null || input.state() != index || input.source() == null) return false;
    }
    return true;
  }

  private static Optional<SystemRecoveryLearningStep> findCheckpoint(String key) {
    return java.util.Arrays.stream(SystemRecoveryLearningStep.values())
        .filter(step -> step.hintKey().equals(key) && step.riddleKey() != null)
        .filter(SystemRecoveryLoad::isMainPuzzleCheckpoint)
        .findFirst();
  }

  /**
   * Returns whether a step is the only checkpoint written for its main riddle.
   *
   * @param step learning step to classify
   * @return whether the step is a main-riddle checkpoint
   */
  public static boolean isMainPuzzleCheckpoint(SystemRecoveryLearningStep step) {
    return switch (step) {
      case ENERGY_ARRAY,
          MODULE_ARRAY,
          INVENTORY_COUNT,
          TRANSPORT_ARRAY,
          MANUAL_SORTING,
          BUBBLE_SORT_CONDITION,
          ARCHIVE_ACCESS,
          STORAGE_ARRAY,
          SEARCH_PROGRAM,
          SYSTEM_CORE_ACCESS ->
          true;
      default -> false;
    };
  }

  /**
   * Returns whether a newly reached checkpoint may be written by the automatic save system.
   *
   * <p>The initial energy-array step remains a valid legacy/load checkpoint, but it is deliberately
   * excluded here. The first new save is created at {@code MODULE_ARRAY}, after the player has
   * completed the first riddle and inserted the battery.
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

  private static boolean booleanValue(Object value) {
    if (!(value instanceof Boolean booleanValue)) {
      throw new IllegalArgumentException("Expected boolean");
    }
    return booleanValue;
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
