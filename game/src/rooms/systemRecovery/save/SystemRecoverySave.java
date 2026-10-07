package rooms.systemRecovery.save;

import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.game.PreRunConfiguration;
import engine.utils.JsonHandler;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import feature.inventory.items.ItemKey;
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
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SortProgramStickItem;
import rooms.systemRecovery.items.SystemCoreAccessChipItem;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.computer.MountedPuzzleItems;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.time.SystemRecoveryTimeLimit;
import rooms.systemRecovery.util.SystemRecoveryAchievementTracker;
import rooms.systemRecovery.util.SystemRecoveryAchievements;

/** Writes the small, checkpoint-based save file for System Recovery. */
public final class SystemRecoverySave {

  /** Current JSON schema version. */
  public static final int FORMAT_VERSION = 9;

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
    return capture(checkpoint, runId, trackingConsent, List.of());
  }

  /**
   * Captures a checkpoint without dropping items whose owners have not rejoined yet.
   *
   * @param checkpoint first learning step of the active main riddle
   * @param runId stable ID of the complete playthrough
   * @param trackingConsent nullable consent decision
   * @param pendingInventoryItems items awaiting their named owner after a load
   * @return immutable save data
   */
  public static SaveData capture(
      SystemRecoveryLearningStep checkpoint,
      UUID runId,
      Boolean trackingConsent,
      List<PlayerItemData> pendingInventoryItems) {
    return capture(checkpoint, runId, trackingConsent, pendingInventoryItems, List.of());
  }

  /**
   * Captures a checkpoint while retaining state owned by players who have not rejoined yet.
   *
   * @param checkpoint first learning step of the active main riddle
   * @param runId stable ID of the complete playthrough
   * @param trackingConsent nullable consent decision
   * @param pendingInventoryItems items awaiting their named owner after a load
   * @param pendingPlayerPositions positions awaiting their named owner after a load
   * @return immutable save data
   */
  public static SaveData capture(
      SystemRecoveryLearningStep checkpoint,
      UUID runId,
      Boolean trackingConsent,
      List<PlayerItemData> pendingInventoryItems,
      List<PlayerPositionData> pendingPlayerPositions) {
    if (checkpoint == null || !SystemRecoveryLoad.isMainPuzzleCheckpoint(checkpoint)) {
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
        SystemRecoveryAchievements.snapshot(),
        List.of(SystemRecoveryLevel.acceptedTerminalSources()),
        List.of(SystemRecoveryLevel.memoryWatchArrayEntries()),
        mergePendingPuzzleItems(currentPuzzleItems(), pendingInventoryItems),
        isSystemCoreExitOpen(),
        SystemRecoveryLevel.systemCoreWarningCallAnswered(),
        mergePendingPlayerPositions(currentPlayerPositions(), pendingPlayerPositions),
        Game.playClock().activeMs(),
        SystemRecoveryLevel.remainingSeconds());
  }

  private static List<PlayerPositionData> currentPlayerPositions() {
    return Game.allPlayers()
        .flatMap(
            player ->
                player.fetch(PlayerComponent.class).stream()
                    .map(PlayerComponent::playerName)
                    .filter(name -> name != null && !name.isBlank())
                    .flatMap(
                        name ->
                            player
                                .fetch(PositionComponent.class)
                                .map(PositionComponent::position)
                                .filter(
                                    position ->
                                        !PositionComponent.ILLEGAL_POSITION.equals(position))
                                .map(
                                    position ->
                                        new PlayerPositionData(name, position.x(), position.y()))
                                .stream()))
        .toList();
  }

  static List<PlayerPositionData> mergePendingPlayerPositions(
      List<PlayerPositionData> currentPositions, List<PlayerPositionData> pendingPositions) {
    Map<String, PlayerPositionData> result = new LinkedHashMap<>();
    if (pendingPositions != null) {
      pendingPositions.forEach(position -> result.put(position.playerName(), position));
    }
    if (currentPositions != null) {
      currentPositions.forEach(position -> result.put(position.playerName(), position));
    }
    return List.copyOf(result.values());
  }

  private static List<PlayerItemData> currentPuzzleItems() {
    List<PlayerItemData> inventoryItems =
        Game.allPlayers()
            .flatMap(
                player ->
                    player.fetch(InventoryComponent.class).stream()
                        .flatMap(
                            inventory ->
                                java.util.Arrays.stream(inventory.items())
                                    .filter(java.util.Objects::nonNull)
                                    .map(item -> playerItem(player, item))
                                    .flatMap(Optional::stream)))
            .toList();
    return mergeMountedPuzzleItems(inventoryItems, MountedPuzzleItems.snapshot());
  }

  static List<PlayerItemData> mergeMountedPuzzleItems(
      List<PlayerItemData> inventoryItems, List<MountedPuzzleItems.MountedItem> mountedItems) {
    Map<String, PlayerItemData> result = new LinkedHashMap<>();
    if (inventoryItems != null) {
      inventoryItems.forEach(item -> result.put(itemIdentity(item), item));
    }
    if (mountedItems != null) {
      mountedItems.stream()
          .map(mounted -> playerItem(mounted.playerName(), mounted.item()).orElse(null))
          .filter(java.util.Objects::nonNull)
          .forEach(item -> result.putIfAbsent(itemIdentity(item), item));
    }
    return List.copyOf(result.values());
  }

  static List<PlayerItemData> mergePendingPuzzleItems(
      List<PlayerItemData> currentItems, List<PlayerItemData> pendingItems) {
    Map<String, PlayerItemData> result = new LinkedHashMap<>();
    if (pendingItems != null) {
      pendingItems.forEach(item -> result.put(itemIdentity(item), item));
    }
    if (currentItems != null) {
      currentItems.forEach(item -> result.put(itemIdentity(item), item));
    }
    return List.copyOf(result.values());
  }

  private static String itemIdentity(PlayerItemData item) {
    return String.valueOf(item.playerName()) + "\u0000" + item.itemKey();
  }

  private static Optional<PlayerItemData> playerItem(engine.Entity player, Item item) {
    String playerName =
        player.fetch(PlayerComponent.class).map(PlayerComponent::playerName).orElse(null);
    return playerItem(playerName, item);
  }

  private static Optional<PlayerItemData> playerItem(String playerName, Item item) {
    if (item instanceof SortProgramStickItem stick) {
      return Optional.of(
          new PlayerItemData(playerName, "sort-program-stick", stick.programmed(), stick.draft()));
    }
    if (item instanceof SearchProgramChipItem chip) {
      return Optional.of(
          new PlayerItemData(playerName, "search-program-chip", chip.programmed(), chip.draft()));
    }
    if (item instanceof SystemCoreAccessChipItem) {
      return Optional.of(new PlayerItemData(playerName, "system-core-access", false, ""));
    }
    if (item instanceof ItemKey) {
      return Optional.of(new PlayerItemData(playerName, "archive-key", false, ""));
    }
    return Optional.empty();
  }

  private static boolean isSystemCoreExitOpen() {
    return SystemRecoveryLevel.systemCoreExitOpen();
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
    root.put("activeMs", data.activeMs());
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
    root.put("terminalHistory", data.terminalHistory());
    root.put("memoryWatch", data.memoryWatchEntries());
    root.put("inventoryItems", data.inventoryItems().stream().map(PlayerItemData::toMap).toList());
    root.put("players", data.playerPositions().stream().map(PlayerPositionData::toMap).toList());
    root.put("systemCoreExitOpen", data.systemCoreExitOpen());
    root.put("systemCoreWarningCallAnswered", data.systemCoreWarningCallAnswered());
    root.put("remainingSeconds", data.remainingSeconds());
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
    map.put("phoneAnsweredBeforeMainQuestCall", progress.phoneAnsweredBeforeMainQuestCall());
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
   * @param terminalHistory accepted source history shown in the terminal UI
   * @param memoryWatchEntries array names, types and values shown in Memory Watch
   * @param inventoryItems puzzle items carried by each named player
   * @param systemCoreExitOpen whether ECHO's final call has already opened the elevator
   * @param systemCoreWarningCallAnswered whether the player has completed ECHO's core warning call
   * @param playerPositions saved world position for each named player
   * @param activeMs active play time captured with this checkpoint
   * @param remainingSeconds remaining shared countdown, excluding time outside the running game
   */
  public record SaveData(
      String checkpointKey,
      List<AcceptedInput> acceptedTerminalInputs,
      List<QuestLogEntryData> questLog,
      UUID runId,
      String playerName,
      Boolean trackingConsent,
      SystemRecoveryAchievementTracker.Snapshot achievementProgress,
      List<String> terminalHistory,
      List<String> memoryWatchEntries,
      List<PlayerItemData> inventoryItems,
      boolean systemCoreExitOpen,
      boolean systemCoreWarningCallAnswered,
      List<PlayerPositionData> playerPositions,
      long activeMs,
      int remainingSeconds) {

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
          achievementProgress,
          terminalHistory,
          memoryWatchEntries,
          inventoryItems,
          systemCoreExitOpen,
          systemCoreWarningCallAnswered,
          playerPositions,
          activeMs,
          remainingSeconds);
    }

    /**
     * Returns this checkpoint with the last safely committed item state.
     *
     * @param items item snapshot to preserve
     * @return copied checkpoint with the supplied item snapshot
     */
    public SaveData withInventoryItems(List<PlayerItemData> items) {
      return new SaveData(
          checkpointKey,
          acceptedTerminalInputs,
          questLog,
          runId,
          playerName,
          trackingConsent,
          achievementProgress,
          terminalHistory,
          memoryWatchEntries,
          items,
          systemCoreExitOpen,
          systemCoreWarningCallAnswered,
          playerPositions,
          activeMs,
          remainingSeconds);
    }

    /**
     * Returns this checkpoint with the current named player positions.
     *
     * @param positions saved positions
     * @return copied checkpoint with the supplied positions
     */
    public SaveData withPlayerPositions(List<PlayerPositionData> positions) {
      return new SaveData(
          checkpointKey,
          acceptedTerminalInputs,
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
          positions,
          activeMs,
          remainingSeconds);
    }

    /**
     * Keeps callers that predate saved player positions source-compatible; the checkpoint starts
     * with the full time limit.
     *
     * @param checkpointKey stable first-step key of the active riddle
     * @param acceptedTerminalInputs accepted terminal inputs
     * @param questLog shared quest-log entries
     * @param runId stable playthrough ID
     * @param playerName saved player name
     * @param trackingConsent run-level tracking decision
     * @param achievementProgress run-local achievement state
     * @param terminalHistory accepted source history shown in the terminal UI
     * @param memoryWatchEntries array entries shown in Memory Watch
     * @param inventoryItems puzzle items carried by each named player
     * @param systemCoreExitOpen whether ECHO's final call has opened the elevator
     * @param systemCoreWarningCallAnswered whether the core warning call was completed
     */
    public SaveData(
        String checkpointKey,
        List<AcceptedInput> acceptedTerminalInputs,
        List<QuestLogEntryData> questLog,
        UUID runId,
        String playerName,
        Boolean trackingConsent,
        SystemRecoveryAchievementTracker.Snapshot achievementProgress,
        List<String> terminalHistory,
        List<String> memoryWatchEntries,
        List<PlayerItemData> inventoryItems,
        boolean systemCoreExitOpen,
        boolean systemCoreWarningCallAnswered) {
      this(
          checkpointKey,
          acceptedTerminalInputs,
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
          List.of(),
          Game.playClock().activeMs(),
          SystemRecoveryTimeLimit.TOTAL_SECONDS);
    }

    /**
     * Backwards-compatible constructor for callers that have not supplied the view snapshots.
     *
     * @param checkpointKey stable key of the active checkpoint
     * @param acceptedTerminalInputs accepted terminal source history
     * @param questLog shared quest-log entries
     * @param runId stable playthrough ID
     * @param playerName saved player name
     * @param trackingConsent run-level tracking decision
     * @param achievementProgress run-local achievement state
     */
    public SaveData(
        String checkpointKey,
        List<AcceptedInput> acceptedTerminalInputs,
        List<QuestLogEntryData> questLog,
        UUID runId,
        String playerName,
        Boolean trackingConsent,
        SystemRecoveryAchievementTracker.Snapshot achievementProgress) {
      this(
          checkpointKey,
          acceptedTerminalInputs,
          questLog,
          runId,
          playerName,
          trackingConsent,
          achievementProgress,
          List.of(),
          List.of(),
          List.of(),
          false,
          false);
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
     * Keeps callers from save format 6 source-compatible.
     *
     * @param checkpointKey stable first-step key of the active main riddle
     * @param acceptedTerminalInputs accepted terminal sources in order
     * @param questLog shared quest-log entries
     * @param runId stable ID shared by every loaded continuation of this save
     * @param playerName authoritative player name stored for the continue flow
     * @param trackingConsent run-level tracking decision; nullable for legacy saves
     * @param achievementProgress run-local achievement conditions; nullable for legacy saves
     * @param terminalHistory accepted source history shown in the terminal UI
     * @param memoryWatchEntries array names, types and values shown in Memory Watch
     * @param inventoryItems puzzle items carried by each named player
     * @param systemCoreExitOpen whether ECHO's final call has opened the elevator
     */
    public SaveData(
        String checkpointKey,
        List<AcceptedInput> acceptedTerminalInputs,
        List<QuestLogEntryData> questLog,
        UUID runId,
        String playerName,
        Boolean trackingConsent,
        SystemRecoveryAchievementTracker.Snapshot achievementProgress,
        List<String> terminalHistory,
        List<String> memoryWatchEntries,
        List<PlayerItemData> inventoryItems,
        boolean systemCoreExitOpen) {
      this(
          checkpointKey,
          acceptedTerminalInputs,
          questLog,
          runId,
          playerName,
          trackingConsent,
          achievementProgress,
          terminalHistory,
          memoryWatchEntries,
          inventoryItems,
          systemCoreExitOpen,
          false);
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
     * @param terminalHistory accepted source history shown in the terminal UI
     * @param memoryWatchEntries array names, types and values shown in Memory Watch
     * @param inventoryItems puzzle items carried by each named player
     * @param systemCoreExitOpen whether the final call has opened the elevator
     * @param systemCoreWarningCallAnswered whether ECHO's core warning call was completed
     * @param playerPositions saved world position for each named player
     * @param activeMs active play time captured with this checkpoint
     * @param remainingSeconds remaining time in seconds, between zero and one hour
     */
    public SaveData {
      if (activeMs < 0) throw new IllegalArgumentException("activeMs must not be negative");
      if (checkpointKey == null || checkpointKey.isBlank()) {
        throw new IllegalArgumentException("checkpointKey must not be blank");
      }
      if (runId == null) throw new IllegalArgumentException("runId must not be null");
      if (remainingSeconds < 0 || remainingSeconds > SystemRecoveryTimeLimit.TOTAL_SECONDS) {
        throw new IllegalArgumentException("Invalid remaining time");
      }
      if (playerName != null && playerName.isBlank()) playerName = null;
      acceptedTerminalInputs =
          List.copyOf(acceptedTerminalInputs == null ? List.of() : acceptedTerminalInputs);
      questLog = List.copyOf(questLog == null ? List.of() : questLog);
      terminalHistory = List.copyOf(terminalHistory == null ? List.of() : terminalHistory);
      memoryWatchEntries = List.copyOf(memoryWatchEntries == null ? List.of() : memoryWatchEntries);
      inventoryItems = List.copyOf(inventoryItems == null ? List.of() : inventoryItems);
      playerPositions = List.copyOf(playerPositions == null ? List.of() : playerPositions);
    }
  }

  /**
   * The last saved world position of one player.
   *
   * @param playerName player identity used to find the entity after joining
   * @param x horizontal world coordinate
   * @param y vertical world coordinate
   */
  public record PlayerPositionData(String playerName, float x, float y) {
    /**
     * Validates the player identity and coordinates.
     *
     * @param playerName player identity used to find the entity after joining
     * @param x horizontal world coordinate
     * @param y vertical world coordinate
     */
    public PlayerPositionData {
      if (playerName == null || playerName.isBlank() || !Float.isFinite(x) || !Float.isFinite(y)) {
        throw new IllegalArgumentException("Invalid saved player position");
      }
    }

    Map<String, Object> toMap() {
      return Map.of("playerName", playerName, "x", x, "y", y);
    }
  }

  /**
   * One puzzle-relevant item held by a named player at the checkpoint.
   *
   * @param playerName inventory owner
   * @param itemKey stable item kind key
   * @param programmed whether the item contains its uploaded program
   * @param draft values currently entered into the program editor's blanks
   */
  public record PlayerItemData(
      String playerName, String itemKey, boolean programmed, String draft) {

    /**
     * Keeps callers and save fixtures from earlier formats source-compatible.
     *
     * @param playerName inventory owner
     * @param itemKey stable item kind key
     * @param programmed whether the item contains its uploaded program
     */
    public PlayerItemData(String playerName, String itemKey, boolean programmed) {
      this(playerName, itemKey, programmed, "");
    }

    /**
     * Normalizes missing draft data from older save formats.
     *
     * @param playerName inventory owner
     * @param itemKey stable item kind key
     * @param programmed whether the item contains its uploaded program
     * @param draft values currently entered into the program editor's blanks
     */
    public PlayerItemData {
      draft = draft == null ? "" : draft;
    }

    Map<String, Object> toMap() {
      Map<String, Object> map = new LinkedHashMap<>();
      if (playerName != null && !playerName.isBlank()) map.put("playerName", playerName);
      map.put("itemKey", itemKey);
      map.put("programmed", programmed);
      map.put("draft", draft);
      return map;
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
