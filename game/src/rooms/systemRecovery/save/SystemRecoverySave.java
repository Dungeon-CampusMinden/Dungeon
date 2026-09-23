package rooms.systemRecovery.save;

import engine.Game;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
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
import java.util.Optional;
import java.util.UUID;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.util.SystemRecoveryAchievementTracker;
import rooms.systemRecovery.util.SystemRecoveryAchievements;

/** Writes the small, checkpoint-based save file for System Recovery. */
public final class SystemRecoverySave {

  /** Current JSON schema version. */
  public static final int FORMAT_VERSION = 4;

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
    return capture(checkpoint, UUID.randomUUID());
  }

  /**
   * Captures a checkpoint while retaining the stable run ID across later loads.
   *
   * @param checkpoint first learning step of the active main riddle
   * @param runId stable ID of the complete playthrough
   * @return immutable save data
   */
  public static SaveData capture(SystemRecoveryLearningStep checkpoint, UUID runId) {
    return capture(checkpoint, runId, null);
  }

  /**
   * Captures a checkpoint and persists the run-level tracking decision.
   *
   * @param checkpoint first learning step of the active main riddle
   * @param runId stable ID of the complete playthrough
   * @param trackingConsent nullable consent decision; null means not decided yet
   * @return immutable save data
   */
  public static SaveData capture(
      SystemRecoveryLearningStep checkpoint, UUID runId, Boolean trackingConsent) {
    if (checkpoint == null || checkpoint.riddleKey() == null) {
      throw new IllegalArgumentException("A learning checkpoint is required.");
    }
    if (runId == null) throw new IllegalArgumentException("A run ID is required.");
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
                            entries.forEach(
                                entry -> questLog.add(QuestLogEntryData.from(tab, entry)))));
    return new SaveData(
        checkpoint.hintKey(),
        inputs,
        questLog,
        runId,
        currentPlayerName(),
        trackingConsent,
        SystemRecoveryAchievements.snapshot());
  }

  private static String currentPlayerName() {
    Optional<String> authoritativeName =
        Game.allPlayers()
            .flatMap(player -> player.fetch(PlayerComponent.class).stream())
            .map(PlayerComponent::playerName)
            .filter(name -> name != null && !name.isBlank())
            .findFirst();
    if (authoritativeName.isPresent()) return authoritativeName.orElseThrow();

    // A network server must wait for a connected player instead of persisting its JVM default.
    if (PreRunConfiguration.multiplayerEnabled()) return null;
    return PreRunConfiguration.username();
  }

  /** Deletes the default checkpoint so a confirmed new game starts from a clean save state. */
  public static void delete() throws IOException {
    Files.deleteIfExists(DEFAULT_PATH);
  }

  /**
   * Updates the consent decision in an existing checkpoint without changing its game progress.
   *
   * @param consent new run-level decision
   * @throws IOException if the checkpoint cannot be rewritten
   */
  public static void updateTrackingConsent(Boolean consent) throws IOException {
    if (consent == null) return;
    Optional<SaveData> existing = SystemRecoveryLoad.read();
    if (existing.isPresent()) write(existing.orElseThrow().withTrackingConsent(consent));
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
          temporary, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
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
    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("runId", data.runId().toString());
    if (data.playerName() != null && !data.playerName().isBlank()) {
      metadata.put("playerName", data.playerName());
    }
    if (data.trackingConsent() != null) {
      metadata.put("trackingConsent", data.trackingConsent());
    }
    root.put("metadata", metadata);
    root.put(
        "acceptedTerminalInputs",
        data.acceptedTerminalInputs().stream()
            .map(input -> Map.of("state", input.state(), "source", input.source()))
            .toList());
    root.put("questLog", data.questLog().stream().map(QuestLogEntryData::toMap).toList());
    if (data.achievementProgress() != null) {
      root.put("achievementProgress", achievementProgressMap(data.achievementProgress()));
    }
    return JsonHandler.writeJson(root, true);
  }

  private static Map<String, Object> achievementProgressMap(
      SystemRecoveryAchievementTracker.Snapshot progress) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("debugRun", progress.debugRun());
    map.put("firstTerminalAttemptSeen", progress.firstTerminalAttemptSeen());
    map.put("wrongTerminalAttempts", progress.wrongTerminalAttempts());
    map.put("acceptedHints", progress.acceptedHints());
    map.put("hintedPuzzles", progress.hintedPuzzles());
    map.put("solvedPuzzles", progress.solvedPuzzles());
    map.put("failedPuzzles", progress.failedPuzzles());
    map.put("failedTerminalPuzzles", progress.failedTerminalPuzzles());
    map.put("failedUploads", progress.failedUploads());
    map.put("acceptedUploads", progress.acceptedUploads());
    map.put("emittedAchievements", progress.emittedAchievements());
    return map;
  }

  /**
   * Data written by one checkpoint save.
   *
   * @param checkpointKey stable first-step key of the active riddle
   * @param acceptedTerminalInputs accepted terminal sources in order
   * @param questLog shared quest-log entries, including player-created notes
   * @param runId stable ID shared by every loaded continuation of this save
   * @param playerName authoritative player name stored for the continue flow
   * @param trackingConsent run-level tracking decision; nullable for legacy or undecided saves
   * @param achievementProgress run-local achievement conditions; nullable for legacy saves
   */
  public record SaveData(
      String checkpointKey,
      List<AcceptedInput> acceptedTerminalInputs,
      List<QuestLogEntryData> questLog,
      UUID runId,
      String playerName,
      Boolean trackingConsent,
      SystemRecoveryAchievementTracker.Snapshot achievementProgress) {

    /**
     * Returns this checkpoint with a replaced run-level tracking decision.
     *
     * @param consent new run-level tracking decision
     * @return copied checkpoint with the new decision
     */
    public SaveData withTrackingConsent(Boolean consent) {
      return new SaveData(
          checkpointKey,
          acceptedTerminalInputs,
          questLog,
          runId,
          playerName,
          consent,
          achievementProgress);
    }

    /**
     * Validates and defensively copies the save collections.
     *
     * @param checkpointKey stable first-step key of the active riddle
     * @param acceptedTerminalInputs accepted terminal sources in order
     * @param questLog shared quest-log entries, including player-created notes
     */
    public SaveData(
        String checkpointKey,
        List<AcceptedInput> acceptedTerminalInputs,
        List<QuestLogEntryData> questLog) {
      this(checkpointKey, acceptedTerminalInputs, questLog, UUID.randomUUID(), null, null);
    }

    /**
     * Validates and defensively copies the save collections.
     *
     * @param checkpointKey stable first-step key of the active main riddle
     * @param acceptedTerminalInputs accepted terminal sources in order
     * @param questLog shared quest-log entries, including player-created notes
     * @param achievementProgress run-local achievement conditions; nullable for legacy saves
     */
    public SaveData(
        String checkpointKey,
        List<AcceptedInput> acceptedTerminalInputs,
        List<QuestLogEntryData> questLog,
        SystemRecoveryAchievementTracker.Snapshot achievementProgress) {
      this(
          checkpointKey,
          acceptedTerminalInputs,
          questLog,
          UUID.randomUUID(),
          null,
          null,
          achievementProgress);
    }

    /**
     * Creates save data with an explicit run ID and no stored player name.
     *
     * @param checkpointKey stable first-step key of the active main riddle
     * @param acceptedTerminalInputs accepted terminal sources in order
     * @param questLog shared quest-log entries, including player-created notes
     * @param runId stable ID shared by every loaded continuation of this save
     * @param achievementProgress run-local achievement conditions; nullable for legacy saves
     */
    public SaveData(
        String checkpointKey,
        List<AcceptedInput> acceptedTerminalInputs,
        List<QuestLogEntryData> questLog,
        UUID runId,
        SystemRecoveryAchievementTracker.Snapshot achievementProgress) {
      this(checkpointKey, acceptedTerminalInputs, questLog, runId, null, achievementProgress);
    }

    /**
     * Creates save data with an explicit run ID, player name and achievement state.
     *
     * @param checkpointKey stable first-step key of the active main riddle
     * @param acceptedTerminalInputs accepted terminal sources in order
     * @param questLog shared quest-log entries, including player-created notes
     * @param runId stable ID shared by every loaded continuation of this save
     * @param playerName authoritative player name stored for the continue flow
     * @param achievementProgress run-local achievement conditions; nullable for legacy saves
     */
    public SaveData(
        String checkpointKey,
        List<AcceptedInput> acceptedTerminalInputs,
        List<QuestLogEntryData> questLog,
        UUID runId,
        String playerName,
        SystemRecoveryAchievementTracker.Snapshot achievementProgress) {
      this(
          checkpointKey,
          acceptedTerminalInputs,
          questLog,
          runId,
          playerName,
          null,
          achievementProgress);
    }

    /**
     * Validates the stable identity and defensively copies all save collections.
     *
     * @param checkpointKey stable first-step key of the active main riddle
     * @param acceptedTerminalInputs accepted terminal sources in order
     * @param questLog shared quest-log entries, including player-created notes
     * @param runId stable ID shared by every loaded continuation of this save
     * @param playerName authoritative player name stored for the continue flow
     * @param trackingConsent run-level tracking decision; nullable for legacy or undecided saves
     * @param achievementProgress run-local achievement conditions; nullable for legacy saves
     */
    public SaveData {
      if (checkpointKey == null || checkpointKey.isBlank()) {
        throw new IllegalArgumentException("checkpointKey must not be blank");
      }
      if (runId == null) throw new IllegalArgumentException("runId must not be null");
      if (playerName != null && playerName.isBlank()) playerName = null;
      acceptedTerminalInputs =
          List.copyOf(acceptedTerminalInputs == null ? List.of() : acceptedTerminalInputs);
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
