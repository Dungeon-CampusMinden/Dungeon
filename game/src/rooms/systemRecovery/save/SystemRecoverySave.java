package rooms.systemRecovery.save;

import engine.utils.JsonHandler;
import feature.questlog.QuestLogEntry;
import feature.questlog.QuestLogUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;

/** Writes the small, checkpoint-based save file for System Recovery. */
public final class SystemRecoverySave {

  /** Current JSON schema version. */
  public static final int FORMAT_VERSION = 1;

  /** Default save location used by the System Recovery main menu. */
  public static final Path DEFAULT_PATH = Path.of("system-recovery-save.json");

  private SystemRecoverySave() {}

  /**
   * Returns whether a readable-looking save file exists at the default menu location.
   *
   * @return whether the continue action should be offered
   */
  public static boolean exists() {
    return SystemRecoveryLoad.read(DEFAULT_PATH).isPresent();
  }

  /**
   * Captures the current terminal and shared quest-log state for one main-riddle checkpoint.
   *
   * @param checkpoint first learning step of the active main riddle
   * @return immutable save data
   */
  public static SaveData capture(SystemRecoveryLearningStep checkpoint) {
    if (checkpoint == null || checkpoint.riddleKey() == null) {
      throw new IllegalArgumentException("A learning checkpoint is required.");
    }
    List<AcceptedInput> inputs =
        TerminalInterpreter.instance().acceptedInputs().stream()
            .map(input -> new AcceptedInput(input.state(), input.source()))
            .toList();
    List<QuestLogEntryData> questLog = new ArrayList<>();
    QuestLogUtil.getQuestLogComponent()
        .ifPresent(
            component ->
                component
                    .getEntries()
                    .forEach(
                        (tab, entries) ->
                            entries.forEach(entry -> questLog.add(QuestLogEntryData.from(tab, entry)))));
    return new SaveData(checkpoint.hintKey(), inputs, questLog);
  }

  /**
   * Writes a save atomically so a process interruption cannot leave a half-written checkpoint.
   *
   * @param path destination file
   * @param data data to write
   * @throws IOException if the file cannot be written
   */
  public static void write(Path path, SaveData data) throws IOException {
    if (path == null || data == null) {
      throw new IllegalArgumentException("Save path and data are required.");
    }
    Path absolute = path.toAbsolutePath();
    Path parent = absolute.getParent();
    if (parent != null) Files.createDirectories(parent);
    Path temporary =
        absolute.resolveSibling(absolute.getFileName() + ".tmp-" + Thread.currentThread().getId());
    Files.writeString(temporary, toJson(data), StandardCharsets.UTF_8);
    try {
      Files.move(
          temporary,
          absolute,
          StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException ignored) {
      Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Writes the default menu save location.
   *
   * @param data data to write
   * @throws IOException if the file cannot be written
   */
  public static void write(SaveData data) throws IOException {
    write(DEFAULT_PATH, data);
  }

  /**
   * Converts save data into the documented JSON representation.
   *
   * @param data data to serialize
   * @return formatted JSON representation
   */
  static String toJson(SaveData data) {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("formatVersion", FORMAT_VERSION);
    root.put("checkpoint", data.checkpointKey());
    root.put(
        "acceptedTerminalInputs",
        data.acceptedTerminalInputs().stream()
            .map(
                input ->
                    Map.of("state", input.state(), "source", input.source()))
            .toList());
    root.put(
        "questLog",
        data.questLog().stream()
            .map(QuestLogEntryData::toMap)
            .toList());
    return JsonHandler.writeJson(root, true);
  }

  /**
   * Data written by one checkpoint save.
   *
   * @param checkpointKey stable first-step key of the active riddle
   * @param acceptedTerminalInputs accepted terminal sources in order
   * @param questLog shared quest-log entries
   */
  public record SaveData(
      String checkpointKey,
      List<AcceptedInput> acceptedTerminalInputs,
      List<QuestLogEntryData> questLog) {
    /**
     * Validates and defensively copies the save collections.
     *
     * @param checkpointKey stable first-step key of the active riddle
     * @param acceptedTerminalInputs accepted terminal sources in order
     * @param questLog shared quest-log entries
     */
    public SaveData {
      if (checkpointKey == null || checkpointKey.isBlank()) {
        throw new IllegalArgumentException("checkpointKey must not be blank");
      }
      acceptedTerminalInputs =
          List.copyOf(
              acceptedTerminalInputs == null ? List.of() : acceptedTerminalInputs);
      questLog = List.copyOf(questLog == null ? List.of() : questLog);
    }
  }

  /**
   * Stable terminal source entry stored independently of localized UI text.
   *
   * @param state interpreter state that accepted the source
   * @param source exact source submitted by the player
   */
  public record AcceptedInput(int state, String source) {}

  /**
   * Serialized shared quest-log entry; text is kept exactly as the network sees it.
   *
   * @param tab quest-log tab key
   * @param text transport text or translation key
   * @param timestamp original quest-log timestamp
   * @param userCreated whether the entry came from a user note
   * @param owner entry owner
   * @param onlyForCreator whether the entry is private
   */
  public record QuestLogEntryData(
      String tab,
      String text,
      int timestamp,
      boolean userCreated,
      String owner,
      boolean onlyForCreator) {
    static QuestLogEntryData from(String tab, QuestLogEntry entry) {
      return new QuestLogEntryData(
          tab,
          entry.text(),
          entry.timestamp(),
          entry.userCreated(),
          entry.owner(),
          entry.onlyForCreator());
    }

    Map<String, Object> toMap() {
      Map<String, Object> map = new LinkedHashMap<>();
      map.put("tab", tab);
      map.put("text", text);
      map.put("timestamp", timestamp);
      map.put("userCreated", userCreated);
      map.put("owner", owner);
      map.put("onlyForCreator", onlyForCreator);
      return map;
    }
  }
}
