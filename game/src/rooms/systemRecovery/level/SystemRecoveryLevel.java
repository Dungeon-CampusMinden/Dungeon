package rooms.systemRecovery.level;

import engine.Entity;
import engine.Game;
import engine.components.InputComponent;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import engine.utils.Vector2;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.CollideComponent;
import feature.components.DecoComponent;
import feature.components.InventoryComponent;
import feature.components.ItemComponent;
import feature.entities.MiscFactory;
import feature.entities.WorldItemBuilder;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hints.HintSystem;
import feature.hud.dialogs.DialogFactory;
import feature.inventory.items.ItemKey;
import feature.systems.LevelEditorSystem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.entities.SystemRecoveryRoomFactory;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.modules.display.DoorLabelComponent;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.riddles.BubbleSortRiddle;
import rooms.systemRecovery.riddles.DataArchiveRiddle;
import rooms.systemRecovery.riddles.EnergyRiddle;
import rooms.systemRecovery.riddles.InventoryScannerRiddle;
import rooms.systemRecovery.riddles.ManualSortingRiddle;
import rooms.systemRecovery.riddles.ModuleStorageRiddle;
import rooms.systemRecovery.riddles.SearchRobotRiddle;
import rooms.systemRecovery.riddles.SystemCoreRiddle;
import rooms.systemRecovery.riddles.TransportStorageRiddle;
import rooms.systemRecovery.riddles.TwoDimensionalStorageRiddle;
import rooms.systemRecovery.save.SystemRecoveryLoad;
import rooms.systemRecovery.save.SystemRecoverySave;
import rooms.systemRecovery.story.SystemRecoveryDialogTriggers;
import rooms.systemRecovery.story.SystemRecoveryPhoneController;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoveryAchievementTracker;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.SystemRecoveryMemoryWatch;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.interpreter.InterpretationCallbacks;
import rooms.systemRecovery.util.interpreter.SystemRecoveryTerminalController;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;
import rooms.systemRecovery.util.interpreter.TerminalStep;
import rooms.systemRecovery.util.shaders.SystemRecoveryAlarm;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents;

/**
 * Builds System Recovery in room order and owns one controller per riddle.
 *
 * <p>Terminal callbacks and snapshot exporters use the static forwarding methods below; mutable
 * puzzle state belongs to this level instance, never to static fields.
 */
public class SystemRecoveryLevel extends DungeonLevel {
  private static final String LEVEL_NAME = "system-recovery-1";
  private static final String TERMINAL_POINT = SystemRecoveryPointRegistry.TERMINAL;
  private static final int PLAYER_INVENTORY_SIZE = 1;
  private final EnergyRiddle energy =
      new EnergyRiddle(this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.ENERGY));
  private final ModuleStorageRiddle moduleStorage =
      new ModuleStorageRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.MODULE_STORAGE));
  private final InventoryScannerRiddle inventoryScanner =
      new InventoryScannerRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.INVENTORY_SCANNER));
  private final TransportStorageRiddle transportStorage =
      new TransportStorageRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.TRANSPORT_STORAGE));
  private final BubbleSortRiddle bubbleSort =
      new BubbleSortRiddle(
          this,
          transportStorage,
          SystemRecoveryPuzzleEvents.forPuzzleCompleting(
              SystemRecoveryPuzzle.BUBBLE_SORT,
              "sort-machine",
              "run",
              SystemRecoveryLearningStep.BUBBLE_SORT_MACHINE));
  private final ManualSortingRiddle manualSorting =
      new ManualSortingRiddle(
          this,
          bubbleSort,
          SystemRecoveryPuzzleEvents.forPuzzleCompleting(
              SystemRecoveryPuzzle.MANUAL_SORTING,
              "sort-display",
              "choice",
              SystemRecoveryLearningStep.MANUAL_SORTING));
  private final DataArchiveRiddle dataArchive =
      new DataArchiveRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.DATA_ARCHIVE));
  private final TwoDimensionalStorageRiddle twoDimensionalStorage =
      new TwoDimensionalStorageRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.TWO_DIMENSIONAL_STORAGE));
  private final SearchRobotRiddle searchRobot =
      new SearchRobotRiddle(
          this,
          SystemRecoveryPuzzleEvents.forPuzzleCompleting(
              SystemRecoveryPuzzle.SEARCH_ROBOT,
              "search-controller",
              "chip",
              SystemRecoveryLearningStep.SEARCH_ROBOT_RUN));
  private final SearchRobotRiddle systemCoreSearchRobot = new SearchRobotRiddle(this);
  private final SystemCoreRiddle systemCore = new SystemCoreRiddle(this, systemCoreSearchRobot);
  private final List<Entity> doorLabels = new ArrayList<>();
  private final SystemRecoveryStoryDialogs storyDialogs = new SystemRecoveryStoryDialogs();
  private final SystemRecoveryPhoneController phoneController =
      new SystemRecoveryPhoneController(
          this::openDataStorageAfterEchoCall, this::openElevatorAfterFinalCall);
  private final Set<Integer> introShownPlayers = new HashSet<>();
  private final Set<Integer> controlsShownPlayers = new HashSet<>();
  private final Set<String> triggeredDialogPoints = new HashSet<>();
  private final SystemRecoveryMemoryWatch memoryWatch = new SystemRecoveryMemoryWatch();
  private Map<String, Point> resolvedPoints = Map.of();
  private final SystemRecoveryTerminalController terminalController =
      new SystemRecoveryTerminalController();
  private final SystemRecoveryRiddleRegistry riddleRegistry =
      new SystemRecoveryRiddleRegistry(
          energy,
          moduleStorage,
          inventoryScanner,
          transportStorage,
          dataArchive,
          twoDimensionalStorage,
          systemCore,
          storyDialogs,
          this::completeSystemCoreRiddleInternal);
  private boolean terminalsUnlocked;
  private boolean systemCoreAccessModuleDelivered;
  private boolean systemCoreAccessGranted;
  private boolean systemCoreAlarmActive;
  private boolean systemCoreRiddleCompleted;
  private boolean endingTriggered;
  private boolean introSuppressed;
  private SystemRecoveryLearningStep savedCheckpoint;
  private List<SystemRecoverySave.QuestLogEntryData> savedQuestLog = List.of();
  private SystemRecoveryAchievementTracker.Snapshot savedAchievementProgress;
  private int saveRevision;
  private Optional<SystemRecoverySave.SaveData> pendingSave = Optional.empty();
  private UUID runId;
  private boolean initialTerminalAttemptRecorded;
  private boolean initialTerminalAttemptWasCorrect;

  /**
   * Creates the System Recovery level.
   *
   * @param layout the tile layout loaded from the level asset
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level asset
   * @param decorations static decorations loaded from the level asset
   */
  public SystemRecoveryLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }

  /**
   * Creates the System Recovery level.
   *
   * @param layout the tile layout loaded from the level asset
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level asset
   */
  public SystemRecoveryLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, LEVEL_NAME);
  }

  @Override
  protected void onFirstTick() {
    resolvedPoints = SystemRecoveryPointRegistry.resolve(this);
    SystemRecoveryAchievements.resetRun(SystemRecovery.debugMode());
    enforcePlayerInventorySize();
    SystemRecoveryAlarm.deactivate();
    SystemRecoveryQuestLogUtil.initializeQuestLog();
    Game.system(HintSystem.class, HintSystem::resetHintProgress);
    pendingSave = SystemRecovery.loadFromSave() ? SystemRecoveryLoad.read() : Optional.empty();
    runId = SystemRecovery.runId();
    SystemRecoveryProgressNet.reset();
    if (pendingSave.isPresent()) {
      // Start from a safe fresh marking. A syntactically valid but semantically corrupt history
      // must not leave the net stranded at a checkpoint when runtime restoration fails.
      SystemRecoveryProgressNet.initializeAtSilently(SystemRecoveryLearningStep.ENERGY_ARRAY);
    } else {
      SystemRecoveryProgressNet.initialize();
    }
    if (!Game.isHeadless() && LevelEditorSystem.active()) {
      terminalsUnlocked = true;
    }
    setupTerminal();
    setupPhone();
    setupRoomLabel();
    closeDoors();
    setupArchiveDoorLock();
    energy.setup();
    moduleStorage.setup();
    inventoryScanner.setup();
    transportStorage.setup();
    manualSorting.setup();
    bubbleSort.setup();
    dataArchive.setup();
    twoDimensionalStorage.setup();
    searchRobot.setup();
    systemCore.setup();
    setupEndTrigger();
    if (restoreSaveIfRequested(pendingSave)) {
      boolean pastIntroduction = savedCheckpoint != SystemRecoveryLearningStep.ENERGY_ARRAY;
      terminalsUnlocked = pastIntroduction;
      introSuppressed = pastIntroduction;
      phoneController.restorePastIntroduction(pastIntroduction);
    }
  }

  @Override
  protected void onTick() {
    enforcePlayerInventorySize();
    saveCheckpointIfNeeded();
    showIntroForNewPlayers();
    triggerDialogPoints();
    storyDialogs.tick();
    if (!Game.isHeadless()) {
      doorLabels.forEach(
          label ->
              label
                  .fetch(DoorLabelComponent.class)
                  .ifPresent(
                      status ->
                          DoorLabelComponent.updateAppearance(
                              label, status.completed().getAsBoolean())));
    }
  }

  /**
   * Restores the validated save after all authoritative room entities exist.
   *
   * @param save save data selected by the server launcher
   * @return whether a save was restored successfully
   */
  private boolean restoreSaveIfRequested(Optional<SystemRecoverySave.SaveData> save) {
    if (!Game.network().isServer() || save.isEmpty()) return false;
    Optional<SystemRecoveryLearningStep> checkpoint =
        save.flatMap(
            data ->
                SystemRecoveryLoad.restoreRuntime(data)
                    .map(
                        restoredCheckpoint -> {
                          SystemRecoveryCheckpointProjection.apply(this, restoredCheckpoint);
                          return restoredCheckpoint;
                        }));
    checkpoint.ifPresent(
        restoredCheckpoint -> {
          SystemRecoverySave.SaveData restoredSave = save.orElseThrow();
          savedCheckpoint = restoredCheckpoint;
          runId = restoredSave.runId();
          savedQuestLog = restoredSave.questLog();
          savedAchievementProgress = restoredSave.achievementProgress();
        });
    if (checkpoint.isPresent()) {
      save.orElseThrow()
          .acceptedTerminalInputs()
          .forEach(input -> memoryWatch.recordAcceptedSource(input.source()));
    }
    return checkpoint.isPresent();
  }

  /** Writes when the checkpoint or any persisted run-local state changes. */
  private void saveCheckpointIfNeeded() {
    if (!Game.network().isServer()) return;
    SystemRecoveryProgressNet.activeStep()
        .filter(SystemRecoveryLoad::isMainPuzzleCheckpoint)
        .ifPresent(
            checkpoint -> {
              SystemRecoverySave.SaveData save = SystemRecoverySave.capture(checkpoint, runId);
              boolean checkpointChanged = checkpoint != savedCheckpoint;
              boolean questLogChanged = !save.questLog().equals(savedQuestLog);
              boolean achievementProgressChanged =
                  !Objects.equals(save.achievementProgress(), savedAchievementProgress);
              if (!checkpointChanged && !questLogChanged && !achievementProgressChanged) return;
              try {
                SystemRecoverySave.write(save);
                savedCheckpoint = checkpoint;
                savedQuestLog = save.questLog();
                savedAchievementProgress = save.achievementProgress();
                saveRevision++;
              } catch (java.io.IOException exception) {
                java.util.logging.Logger.getLogger(SystemRecoveryLevel.class.getName())
                    .warning(
                        "Could not write System Recovery checkpoint: " + exception.getMessage());
              }
            });
  }

  /** Restores the completed energy state without firing puzzle callbacks. */
  void restoreCompletedEnergy() {
    energy.restoreCompletedState();
  }

  void showModuleAssignmentsAfterRestore() {
    moduleStorage.showModuleAssignments();
  }

  void restoreCompletedModules() {
    energy.restoreCompletedState();
    moduleStorage.restoreCompletedState();
    openDoor(SystemRecoveryPointRegistry.DOOR_MODULE_STORAGE);
  }

  void restoreCompletedInventoryScanner() {
    inventoryScanner.restoreCompletedState();
  }

  void restoreCompletedTransport() {
    restoreCompletedModules();
    inventoryScanner.restoreCompletedState();
    transportStorage.restoreCompletedState();
    openDoor(SystemRecoveryPointRegistry.DOOR_INVENTORY_SCANNER);
    openDoor(SystemRecoveryPointRegistry.DOOR_TRANSPORT_STORAGE);
  }

  void restoreCompletedManualSorting() {
    manualSorting.restoreCompletedState();
  }

  void restoreCompletedBubbleSort() {
    restoreCompletedTransport();
    openDoor(SystemRecoveryPointRegistry.DOOR_DATA_STORAGE);
    manualSorting.restoreCompletedState(false);
    bubbleSort.restoreCompletedState();
  }

  void restoreCompletedDataArchive() {
    dataArchive.restoreCompletedState();
  }

  void restoreCompletedStorage() {
    restoreCompletedBubbleSort();
    openDoor(SystemRecoveryPointRegistry.DOOR_DATA_ARCHIVE);
    dataArchive.restoreCompletedState();
    twoDimensionalStorage.restoreCompletedState();
  }

  void restoreCompletedSearchRobot() {
    searchRobot.restoreCompletedState();
  }

  void markSystemCoreAccessModuleDeliveredAfterRestore() {
    systemCoreAccessModuleDelivered = true;
  }

  void openDoor(String pointName) {
    tileAt(point(pointName))
        .filter(DoorTile.class::isInstance)
        .map(DoorTile.class::cast)
        .ifPresent(DoorTile::open);
  }

  void spawnWorldItemIfMissing(feature.inventory.Item item, String pointName) {
    Point spawnPoint = point(pointName);
    boolean alreadyPresent =
        Game.entityAtPoint(spawnPoint)
            .anyMatch(
                entity ->
                    entity
                        .fetch(ItemComponent.class)
                        .map(component -> component.item().getClass() == item.getClass())
                        .orElse(false));
    if (!alreadyPresent) Game.add(WorldItemBuilder.buildWorldItem(item, spawnPoint));
  }

  /**
   * Revision of the most recent checkpoint the server successfully wrote.
   *
   * @return current server save revision, or zero outside this level
   */
  public static int saveRevision() {
    return currentLevel().map(level -> level.saveRevision).orElse(0);
  }

  /** Keeps the System Recovery inventory intentionally limited to one carried item. */
  private void enforcePlayerInventorySize() {
    if (!Game.network().isServer()) return;

    Game.allPlayers()
        .forEach(
            player ->
                player
                    .fetch(InventoryComponent.class)
                    .ifPresent(
                        inventory -> {
                          if (inventory.items().length == PLAYER_INVENTORY_SIZE) return;

                          InventoryComponent singleSlotInventory =
                              new InventoryComponent(PLAYER_INVENTORY_SIZE);
                          for (var item : inventory.items()) {
                            if (item != null) {
                              singleSlotInventory.add(item);
                              break;
                            }
                          }
                          player.remove(InventoryComponent.class);
                          player.add(singleSlotInventory);
                        }));
  }

  /** Sends the room's lore to each player once through the networked dialog system. */
  private void showIntroForNewPlayers() {
    // Do not initialize the graphical level-editor class on the headless authoritative server.
    if (introSuppressed || (!Game.isHeadless() && LevelEditorSystem.active())) return;
    Game.allPlayers()
        .filter(player -> introShownPlayers.add(player.id()))
        .forEach(
            player ->
                BlackFadeCutscene.show(
                    rooms.systemRecovery.util.SystemRecoveryText.introPages(),
                    false,
                    true,
                    true,
                    () -> finishIntroForPlayer(player.id()),
                    player.id()));
  }

  /**
   * Shows the controls immediately after the intro and unlocks the terminal afterwards.
   *
   * @param playerId player receiving the controls dialog
   */
  private void finishIntroForPlayer(int playerId) {
    if (!controlsShownPlayers.add(playerId)) return;
    DialogFactory.showDialogDialog(
        SystemRecoveryText.controls(), () -> terminalsUnlocked = true, playerId);
  }

  /** Spawns the phone and keeps it interactable after every call. */
  private void setupPhone() {
    phoneController.setup(point("phone"));
  }

  /** Starts ECHO's one introductory call after the first rejected terminal attempt. */
  private void triggerEchoCall() {
    phoneController.triggerOpeningCall();
  }

  /** Starts ECHO's warning call after AXIOM has obtained the system-core access module. */
  private void triggerSystemCoreWarningCall() {
    phoneController.triggerSystemCoreWarningCall();
  }

  /** Starts ECHO's transition call after the transport-storage sequence has completed. */
  public static void triggerDataStorageProblemCall() {
    currentLevel().ifPresent(level -> level.phoneController.triggerDataStorageProblemCall());
  }

  /** Starts ECHO's final call after the system-core routines have been completed. */
  private void triggerFinalEchoCall() {
    phoneController.triggerFinalEchoCall();
  }

  /** Opens the data-storage room only after the player has answered ECHO's warning call. */
  private void openDataStorageAfterEchoCall() {
    if (SystemRecoveryProgressNet.activeStep().orElse(null)
        != SystemRecoveryLearningStep.DATA_STORAGE_DOOR_OPEN) {
      return;
    }

    DoorTile storageDoor = (DoorTile) tileAt(point("door_datenspeicher")).orElseThrow();
    storageDoor.open();
    if (storageDoor.isOpen()) {
      SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.DATA_STORAGE_DOOR_OPEN);
    }
  }

  /** Starts ECHO's introductory call after the first rejected terminal input. */
  public static void triggerEchoCallForIncorrectInput() {
    currentLevel()
        .ifPresent(
            level -> {
              level.recordInitialTerminalAttemptInternal(false);
              level.triggerEchoCall();
            });
  }

  /**
   * Records the first energy-terminal attempt and returns whether it was accepted.
   *
   * @param correct whether the submitted attempt was correct
   * @return whether the attempt was recorded as correct
   */
  public static boolean recordInitialTerminalAttempt(boolean correct) {
    return currentLevel()
        .map(level -> level.recordInitialTerminalAttemptInternal(correct))
        .orElse(false);
  }

  private boolean recordInitialTerminalAttemptInternal(boolean correct) {
    if (initialTerminalAttemptRecorded
        || TerminalInterpreter.instance().currentState() != TerminalStep.ENERGY_ARRAY.stateId()) {
      return false;
    }
    initialTerminalAttemptRecorded = true;
    initialTerminalAttemptWasCorrect = correct;
    return correct;
  }

  /** Starts ECHO's successful first-contact call after AXIOM's dialog has closed. */
  public static void triggerEchoCallAfterInitialCorrectInput() {
    currentLevel().ifPresent(SystemRecoveryLevel::triggerCorrectOpeningCall);
  }

  private void triggerCorrectOpeningCall() {
    phoneController.triggerCorrectOpeningCall();
  }

  /**
   * Returns whether the first accepted terminal input was the correct energy-array code.
   *
   * @return whether the first attempt was correct
   */
  public static boolean recordedInitialTerminalAttemptWasCorrect() {
    return currentLevel().map(level -> level.initialTerminalAttemptWasCorrect).orElse(false);
  }

  /** Starts the next instruction when a player reaches a configured room-entry trigger. */
  private void triggerDialogPoints() {
    Game.levelEntities(Set.of(PlayerComponent.class))
        .filter(player -> player.isPresent(engine.components.PositionComponent.class))
        .forEach(
            player ->
                SystemRecoveryDialogTriggers.ROOM_ENTRY.forEach(
                    trigger -> {
                      Point triggerPoint = point(trigger.pointName());
                      Point playerPoint =
                          player
                              .fetch(engine.components.PositionComponent.class)
                              .orElseThrow()
                              .position();
                      if (!isDialogTriggerEnabled(trigger)) return;
                      String triggerKey = trigger.pointName() + ":" + player.id();
                      if (playerPoint.distanceSquared(triggerPoint) <= 1.0
                          && triggeredDialogPoints.add(triggerKey)) {
                        storyDialogs.announceForPlayer(trigger.step(), player.id());
                      }
                    }));
  }

  private boolean isDialogTriggerEnabled(SystemRecoveryDialogTriggers.DialogTrigger trigger) {
    SystemRecoveryLearningStep requiredStep =
        switch (trigger.pointName()) {
          case SystemRecoveryDialogTriggers.MODULE_STORAGE ->
              SystemRecoveryLearningStep.MODULE_ARRAY;
          case SystemRecoveryDialogTriggers.INVENTORY_SCANNER ->
              SystemRecoveryLearningStep.INVENTORY_COUNT;
          case SystemRecoveryDialogTriggers.TRANSPORT_STORAGE ->
              SystemRecoveryLearningStep.TRANSPORT_ARRAY;
          case SystemRecoveryDialogTriggers.MANUAL_SORTING ->
              SystemRecoveryLearningStep.MANUAL_SORTING;
          case SystemRecoveryDialogTriggers.DATA_ARCHIVE ->
              SystemRecoveryLearningStep.ARCHIVE_ARRAYS;
          case SystemRecoveryDialogTriggers.TWO_DIMENSIONAL_STORAGE ->
              SystemRecoveryLearningStep.STORAGE_ARRAY;
          case SystemRecoveryDialogTriggers.SYSTEM_CORE -> SystemRecoveryLearningStep.CORE_SORT;
          default ->
              throw new IllegalArgumentException("Unknown dialog trigger: " + trigger.pointName());
        };
    if (SystemRecoveryProgressNet.activeStep().orElse(null) != requiredStep) return false;
    if (SystemRecoveryDialogTriggers.MODULE_STORAGE.equals(trigger.pointName())) {
      return energy.batteryInserted();
    }
    if (SystemRecoveryDialogTriggers.INVENTORY_SCANNER.equals(trigger.pointName())) {
      return moduleStorage.lengthInspected();
    }
    if (SystemRecoveryDialogTriggers.TRANSPORT_STORAGE.equals(trigger.pointName())) {
      return inventoryScanner.completed();
    }
    if (SystemRecoveryDialogTriggers.MANUAL_SORTING.equals(trigger.pointName())) {
      return transportStorage.completed();
    }
    if (SystemRecoveryDialogTriggers.TWO_DIMENSIONAL_STORAGE.equals(trigger.pointName())) {
      return dataArchive.completed();
    }
    return true;
  }

  private static SystemRecoveryLevel active() {
    return (SystemRecoveryLevel) Game.currentLevel().orElseThrow();
  }

  /**
   * Returns whether the opening call has unlocked the room's computer terminals.
   *
   * @return whether terminals are available
   */
  public static boolean terminalsUnlocked() {
    return currentLevel().map(level -> level.terminalsUnlocked).orElse(false);
  }

  /** Materializes the energy array for riddle 1, terminal step 1. */
  public static void spawnEnergyCrates() {
    active().energy.spawnEnergyCrates();
  }

  /**
   * Runs terminal input with an explicit server-side player context.
   *
   * @param source complete source submitted by the player
   * @param playerId authoritative player ID
   * @return whether the input was accepted
   */
  public static boolean interpretTerminalInput(String source, int playerId) {
    return interpretTerminalInput(source, playerId, null);
  }

  /**
   * Runs terminal input while preserving the submitting dialog for targeted feedback.
   *
   * @param source complete source submitted by the player
   * @param playerId authoritative player ID
   * @param dialogId originating dialog ID, or {@code null}
   * @return whether the input was accepted
   */
  public static boolean interpretTerminalInput(String source, int playerId, String dialogId) {
    if (!terminalsUnlocked()) return false;
    return active().terminalController.interpret(source, playerId, dialogId);
  }

  /**
   * Records an accepted player source for the shared quest log and Memory Watch.
   *
   * @param attempt accepted terminal attempt
   */
  public static void recordAcceptedSolution(TerminalAttempt attempt) {
    if (attempt == null) return;
    currentLevel()
        .ifPresent(
            level -> {
              level.memoryWatch.recordAcceptedSource(attempt.source());
              SystemRecoveryQuestLogUtil.addTerminalSolutionEntry(attempt);
            });
  }

  /**
   * Records an accepted chip program or final result payload for its matching riddle.
   *
   * @param step learning step receiving the solution entry
   * @param source accepted source or payload
   */
  public static void recordAcceptedSolution(SystemRecoveryLearningStep step, String source) {
    currentLevel()
        .ifPresent(
            level -> {
              level.memoryWatch.recordAcceptedSource(source);
              SystemRecoveryQuestLogUtil.addSolutionEntry(step, source);
            });
  }

  /**
   * Returns accepted array identifiers and data types for the Memory Watch tab.
   *
   * @return accepted array entries
   */
  public static String[] memoryWatchArrayEntries() {
    return currentLevel()
        .map(level -> level.memoryWatch.arrayEntries())
        .orElseGet(() -> new String[0]);
  }

  /**
   * Applies a validated terminal step to the riddle instances of the active level.
   *
   * @param step accepted terminal step
   * @param attempt authoritative attempt context
   */
  public static void applyTerminalStep(TerminalStep step, TerminalAttempt attempt) {
    currentLevel().ifPresent(level -> level.riddleRegistry.apply(step, attempt));
  }

  /**
   * Returns whether the final system-core result mask should be available in the computer.
   *
   * @return whether the final input mask is available
   */
  public static boolean systemCoreMetaAvailable() {
    return terminalsUnlocked()
        && TerminalInterpreter.instance().currentState() == TerminalStep.SYSTEM_CORE_META.stateId()
        && SystemRecoveryProgressNet.activeStep().orElse(null)
            == SystemRecoveryLearningStep.CORE_META;
  }

  /**
   * Completes the final System Core robot run after the robot reached every matrix cell.
   *
   * @param playerId player whose accepted program started the scan, or a negative shared context
   */
  public static void completeSystemCoreRobotSearch(int playerId) {
    currentLevel()
        .ifPresent(
            level -> {
              if (SystemRecoveryProgressNet.activeStep().orElse(null)
                  != SystemRecoveryLearningStep.CORE_SEARCH_ROBOT) {
                return;
              }
              level.systemCore.completeMapSearch();
              if (!SystemRecoveryProgressNet.complete(
                  SystemRecoveryLearningStep.CORE_SEARCH_ROBOT)) {
                return;
              }
              if (playerId >= 0) {
                level.storyDialogs.announceForPlayer(
                    SystemRecoveryStoryDialogs.CENTRAL_META, playerId);
              } else {
                level.storyDialogs.announceToAllPlayers(SystemRecoveryStoryDialogs.CENTRAL_META);
              }
            });
  }

  /**
   * Validates and submits the final system-core results from the dedicated input mask.
   *
   * @param payload submitted combination
   * @param playerId authoritative player ID
   * @return whether the combination was accepted
   */
  public static boolean submitSystemCoreMeta(String payload, int playerId) {
    return submitSystemCoreMeta(payload, playerId, null);
  }

  /**
   * Validates the final input mask while preserving the submitting dialog for feedback.
   *
   * @param payload submitted combination
   * @param playerId authoritative player ID
   * @param dialogId originating dialog ID, or {@code null}
   * @return whether the combination was accepted
   */
  public static boolean submitSystemCoreMeta(String payload, int playerId, String dialogId) {
    if (!systemCoreMetaAvailable()) return false;
    int state = TerminalInterpreter.instance().currentState();
    TerminalAttempt attempt = new TerminalAttempt(state, payload, playerId, dialogId);
    if (!active().systemCore.acceptsMetaInput(payload)) {
      InterpretationCallbacks.onIncorrectTerminalInput(attempt);
      return false;
    }
    TerminalInterpreter.instance().synchronizeState(state + 1);
    InterpretationCallbacks.onRiddleTenMetaCombinationCompleted(attempt);
    return true;
  }

  /**
   * Shows the authoritative token state of the System Recovery Petri net to one debug client.
   *
   * @param playerId debug client receiving the snapshot
   */
  public static void showPetriNetDebug(int playerId) {
    if (!SystemRecovery.debugMode()) return;
    DialogFactory.showTextDialog(
        SystemRecoveryProgressNet.debugSnapshot().dialogPayload(),
        SystemRecoveryText.key("computer.debug-petri-net-title"),
        () -> {},
        SystemRecoveryText.key("computer.debug-close"),
        playerId);
  }

  /**
   * Announces one story instruction after a shared physical room sequence.
   *
   * @param step story step to announce
   */
  public static void announceStoryToAllPlayers(SystemRecoveryStoryDialogs.StoryStep step) {
    currentLevel().ifPresent(level -> level.storyDialogs.announceToAllPlayers(step));
  }

  /**
   * Announces one story instruction to one player after a world interaction.
   *
   * @param step story step to announce
   * @param playerId player receiving the instruction
   */
  public static void announceStoryForPlayer(
      SystemRecoveryStoryDialogs.StoryStep step, int playerId) {
    currentLevel().ifPresent(level -> level.storyDialogs.announceForPlayer(step, playerId));
  }

  /** Announces AXIOM's reaction when the search robot delivers the access module. */
  public static void announceSystemCoreAccessModuleDelivered() {
    currentLevel().ifPresent(SystemRecoveryLevel::handleSystemCoreAccessModuleDelivered);
  }

  private void handleSystemCoreAccessModuleDelivered() {
    if (systemCoreAccessModuleDelivered) return;
    systemCoreAccessModuleDelivered = true;
    storyDialogs.announceToAllPlayers(SystemRecoveryStoryDialogs.ACCESS_MODULE_FOUND);
  }

  private static java.util.Optional<SystemRecoveryLevel> currentLevel() {
    return Game.currentLevel()
        .filter(SystemRecoveryLevel.class::isInstance)
        .map(SystemRecoveryLevel.class::cast);
  }

  private void closeDoors() {
    Game.allTiles(LevelElement.DOOR)
        .forEach(
            tile -> {
              ((DoorTile) tile).close();
            });
  }

  /** Locks the archive door with the shared key-and-lock interaction. */
  private void setupArchiveDoorLock() {
    DoorTile archiveDoor = (DoorTile) tileAt(point("door_datenarchiv")).orElseThrow();
    Game.add(
        MiscFactory.createDoorBlocker(
            archiveDoor,
            ItemKey.class,
            player -> {
              if (!archiveDoor.isOpen()) return;
              SystemRecoveryPuzzleEvents.attempt(
                  SystemRecoveryPuzzle.BUBBLE_SORT,
                  "archive-door",
                  "use-sort-key",
                  "key",
                  true,
                  player);
              SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.ARCHIVE_ACCESS);
            }));
  }

  /** Starts the shared ending cutscene when a player reaches the final point after all riddles. */
  private void setupEndTrigger() {
    Entity trigger = new Entity("system-recovery-end-trigger");
    trigger.add(new PositionComponent(point("end")));
    trigger.add(
        new CollideComponent(
                Vector2.ZERO,
                Vector2.ONE,
                (ignored, other, direction) -> {
                  if (!systemCoreRiddleCompleted || endingTriggered) return;
                  if (other.fetch(InputComponent.class).isEmpty()) return;
                  endingTriggered = true;
                  BlackFadeCutscene.show(
                      SystemRecoveryText.endingPages(),
                      true,
                      false,
                      true,
                      Game::complete,
                      other.id());
                },
                null)
            .isSolid(false));
    Game.add(trigger);
  }

  private void setupRoomLabel() {
    addDoorLabel(
        "label_modulspeicher",
        SystemRecoveryText.key("world.labels.module-storage"),
        SystemRecoveryText.key("world.labels.room", 2),
        moduleStorage::completed,
        -90f);
    addDoorLabel(
        "label_inventarscanner",
        SystemRecoveryText.key("world.labels.inventory-scanner"),
        SystemRecoveryText.key("world.labels.room", 3),
        inventoryScanner::completed,
        180f);
    addDoorLabel(
        "label_transportlager",
        SystemRecoveryText.key("world.labels.transport-storage"),
        SystemRecoveryText.key("world.labels.room", 4),
        transportStorage::completed);
    addDoorLabel(
        "label_datenspeicher",
        SystemRecoveryText.key("world.labels.data-storage"),
        SystemRecoveryText.key("world.labels.room", 5),
        manualSorting::completed);
    addDoorLabel(
        "label_sortmachine",
        SystemRecoveryText.key("world.labels.bubble-sort"),
        SystemRecoveryText.key("world.labels.room", 6),
        bubbleSort::completed);
    addDoorLabel(
        "label_archive",
        SystemRecoveryText.key("world.labels.data-archive"),
        SystemRecoveryText.key("world.labels.room", 7),
        dataArchive::completed);
    addDoorLabel(
        "label_speicher",
        SystemRecoveryText.key("world.labels.two-dimensional-storage"),
        SystemRecoveryText.key("world.labels.room", 8),
        twoDimensionalStorage::completed,
        90f);
    addDoorLabel(
        "label_suchroboter",
        SystemRecoveryText.key("world.labels.search-robot"),
        SystemRecoveryText.key("world.labels.room", 9),
        searchRobot::completed);
    addDoorLabel(
        "label_systemcore",
        SystemRecoveryText.key("world.labels.system-core"),
        SystemRecoveryText.key("world.labels.system-core-room"),
        () -> systemCoreRiddleCompleted,
        -90f);
  }

  /**
   * Associates each destination label with its prerequisite, independent of its door lock.
   *
   * @param point label custom-point name
   * @param text localized label text or translation key
   * @param title localized interaction title or translation key
   * @param completed supplier for the prerequisite completion state
   */
  private void addDoorLabel(String point, String text, String title, BooleanSupplier completed) {
    addDoorLabel(point, text, title, completed, 0f);
  }

  /**
   * Adds a room label with an explicit visual orientation.
   *
   * @param point label custom-point name
   * @param text localized label text or translation key
   * @param title localized interaction title or translation key
   * @param completed supplier for the prerequisite completion state
   * @param rotation visual rotation in degrees
   */
  private void addDoorLabel(
      String point, String text, String title, BooleanSupplier completed, float rotation) {
    Entity label = SystemRecoveryRoomFactory.roomLabel(this.point(point), text, title, rotation);
    label.name(point);
    label.add(new DoorLabelComponent(completed));
    DoorLabelComponent.updateAppearance(label, completed.getAsBoolean());
    doorLabels.add(label);
    Game.add(label);
  }

  private void setupTerminal() {
    TerminalInterpreter.instance().reset();
    TerminalInterpreterSetup.setupRoomStates();
    Entity terminal = DecoFactory.createDeco(point(TERMINAL_POINT), Deco.DeskWithPC1);
    terminal.name(TERMINAL_POINT);
    terminal.remove(DecoComponent.class);
    SystemRecoveryComputerFactory.attachComputerDialog(terminal);
    Game.add(terminal);
  }

  /** Activates all module sockets after the module array is initialized. */
  public static void activateModuleSockets() {
    active().moduleStorage.activateModuleSockets();
  }

  /** Shows the module names and their required indices on the room 2 display. */
  public static void showModuleAssignments() {
    active().moduleStorage.showModuleAssignments();
  }

  /** Spawns the five module chips on their corresponding sockets. */
  public static void spawnModuleChips() {
    active().moduleStorage.spawnModuleChips();
  }

  /** Removes the defective GPU chip from the third socket. */
  public static void removeGpuChip() {
    active().moduleStorage.removeGpuChip();
  }

  /** Moves the module chips to the inventory scanner without moving their sockets. */
  public static void portModuleChipsToScanner() {
    active().moduleStorage.portModuleChipsToScanner();
  }

  /** Updates the room display with the module array length. */
  public static void showModuleArrayLength() {
    active().moduleStorage.showModuleArrayLength();
  }

  /** Opens the next room after the data archive arrays have been accepted. */
  public static void completeDataArchive() {
    active().dataArchive.complete();
  }

  /** Enables the scanner lever after the inventory scanner code has been solved. */
  public static void completeScannerPuzzle() {
    active().inventoryScanner.completeScannerPuzzle();
  }

  /**
   * Returns the module index currently being examined by the inventory scanner.
   *
   * @return zero-based module index
   */
  public static int currentScannerModuleIndex() {
    return active().inventoryScanner.currentScanIndex();
  }

  /**
   * Returns whether the inventory scanner has detected the defective GPU.
   *
   * @return whether the GPU fault was detected
   */
  public static boolean scannerFaultDetected() {
    return active().inventoryScanner.scannerFaultDetected();
  }

  /**
   * Returns whether the inventory scanner is currently examining the module row.
   *
   * @return whether the scanner animation is running
   */
  public static boolean scannerRunning() {
    return active().inventoryScanner.running();
  }

  /**
   * Returns the matrix cell currently examined by the search robot, if any.
   *
   * @return current matrix cell as a coordinate string, or an empty string
   */
  public static String currentSearchRobotCell() {
    engine.utils.Point point = active().searchRobot.currentCellPoint();
    return point == null ? "" : point.x() + "," + point.y();
  }

  /**
   * Returns the matrix cell currently examined by the specified synchronized robot entity.
   *
   * @param robotEntity synchronized robot entity to inspect
   * @return current matrix cell as a coordinate string, or an empty string
   */
  public static String currentSearchRobotCell(Entity robotEntity) {
    SystemRecoveryLevel level = active();
    engine.utils.Point point =
        level.searchRobot.controls(robotEntity)
            ? level.searchRobot.currentCellPoint()
            : level.systemCoreSearchRobot.controls(robotEntity)
                ? level.systemCoreSearchRobot.currentCellPoint()
                : null;
    return point == null ? "" : point.x() + "," + point.y();
  }

  /** Spawns the packages for transport riddle four exactly once. */
  public static void spawnTransportPackages() {
    active().transportStorage.spawnTransportPackages();
  }

  /** Starts the authoritative conveyor animation for transport riddle four. */
  public static void startTransportSequence() {
    active().transportStorage.startTransportSequence();
  }

  /**
   * Returns the server-authoritative entity IDs of the pair currently compared by the sorter.
   *
   * <p>The network snapshot translator uses these IDs to reproduce the cyan comparison highlight on
   * every connected client. The sorter state itself is intentionally not accepted from clients.
   *
   * @return two entity IDs, or {@code {-1, -1}} when no comparison is active
   */
  public static int[] currentSortComparisonEntityIds() {
    return active().manualSorting.currentSortComparisonEntityIds();
  }

  /**
   * Returns the IDs of the two packages and the scanner currently active on the conveyor.
   *
   * @return left package, right package and scanner IDs; {@code -1} when idle
   */
  public static int[] currentBeltSortEntityIds() {
    return active().bubbleSort.currentBeltSortEntityIds();
  }

  /**
   * Returns all active conveyor package IDs paired with their authoritative weights.
   *
   * @return serialized package metadata for the current conveyor sequence
   */
  public static String currentBeltPackageMetadata() {
    return active().bubbleSort.currentBeltPackageMetadata();
  }

  /** Enables the array lever after the energy puzzle has been solved. */
  public static void completeEnergyPuzzle() {
    active().energy.completeEnergyPuzzle();
  }

  /** Activates the twelve storage cells after the riddle 8 array declaration. */
  public static void activateStorageMatrix() {
    active().twoDimensionalStorage.activateMatrix();
  }

  /** Shows the three populated coordinates after the riddle 8 assignments. */
  public static void fillStorageMatrix() {
    active().twoDimensionalStorage.fillMatrix();
  }

  /** Completes riddle 8 and starts the authoritative chip delivery animation. */
  public static void completeStorageMatrix() {
    active().twoDimensionalStorage.complete();
  }

  /**
   * Returns the server-authoritative state of one storage matrix cell.
   *
   * @param entityName synchronized matrix-cell entity name
   * @return empty, active, target or filled
   */
  public static String storageCellState(String entityName) {
    String[] parts = entityName.split("_");
    if (parts.length < 5) return "empty";
    try {
      return active()
          .twoDimensionalStorage
          .cellState(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
    } catch (NumberFormatException ignored) {
      return "empty";
    }
  }

  /**
   * Returns whether the system-core access script has been accepted by the server.
   *
   * @return whether system-core access was granted
   */
  public static boolean systemCoreAccessGranted() {
    return active().systemCoreAccessGranted;
  }

  /**
   * Returns whether the final system-core terminal riddle has been solved.
   *
   * @return whether the final riddle is complete
   */
  public static boolean systemCoreRiddleCompleted() {
    return active().systemCoreRiddleCompleted;
  }

  /**
   * Returns the server-authoritative completion stage of the three system-core areas.
   *
   * @return number of completed system-core areas
   */
  public static int systemCoreStage() {
    return active().systemCore.stage();
  }

  /**
   * Returns whether the red system-core alarm should currently be active.
   *
   * @return whether the alarm is active
   */
  public static boolean systemCoreAlarmActive() {
    SystemRecoveryLevel level = active();
    return level.systemCoreAlarmActive && !level.systemCoreRiddleCompleted;
  }

  /** Marks the Bubble Sort section of the central-computer riddle as solved. */
  public static void completeSystemCoreSort() {
    active().systemCore.completeSort();
  }

  /** Marks the module-count section of the central-computer riddle as solved. */
  public static void completeSystemCoreModuleCount() {
    active().systemCore.completeModuleCount();
  }

  /** Marks the matrix-search section of the central-computer riddle as solved. */
  public static void completeSystemCoreMapSearch() {
    active().systemCore.completeMapSearch();
  }

  /** Marks the final system-core terminal riddle as solved. */
  public static void completeSystemCoreRiddle() {
    SystemRecoveryLevel level = active();
    level.completeSystemCoreRiddleInternal();
  }

  private void completeSystemCoreRiddleInternal() {
    if (systemCoreRiddleCompleted) return;
    systemCore.complete();
    systemCoreRiddleCompleted = true;
    systemCoreAlarmActive = false;
    SystemRecoveryAlarm.deactivate();
    SystemRecoveryPuzzleEvents.solved(SystemRecoveryPuzzle.SYSTEM_CORE);
    storyDialogs.announceCompletionToAllPlayers();
    triggerFinalEchoCall();
  }

  /** Opens the shared elevator exit only after ECHO's final call has been answered. */
  private void openElevatorAfterFinalCall() {
    DoorTile elevatorDoor = (DoorTile) tileAt(point("door_elevator")).orElseThrow();
    elevatorDoor.open();
    if (elevatorDoor.isOpen()) systemCore.markExitOpen();
  }

  /**
   * Atomically accepts the system-core access script and opens the system-core door.
   *
   * <p>The progression-place check is intentionally performed before tracking and world changes. A
   * delayed callback from an older computer dialog therefore cannot grant access after the shared
   * progression has moved elsewhere.
   *
   * @param playerId player who executed the script
   * @return whether the access script was accepted
   */
  public static boolean completeSystemCoreAccess(int playerId) {
    SystemRecoveryLevel level = active();
    if (level.systemCoreAccessGranted
        || SystemRecoveryProgressNet.activeStep().orElse(null)
            != SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS) {
      SystemRecoveryPuzzleEvents.attempt(
          SystemRecoveryPuzzle.SYSTEM_CORE, "access-script", "execute", "run", false, playerId);
      return false;
    }
    DoorTile systemCoreDoor = (DoorTile) level.tileAt(level.point("door_systemcore")).orElseThrow();
    systemCoreDoor.open();
    if (!systemCoreDoor.isOpen()
        || !SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS)) {
      SystemRecoveryPuzzleEvents.attempt(
          SystemRecoveryPuzzle.SYSTEM_CORE, "access-script", "execute", "run", false, playerId);
      return false;
    }
    level.systemCoreAccessGranted = true;
    level.systemCoreAlarmActive = true;
    SystemRecoveryPuzzleEvents.attempt(
        SystemRecoveryPuzzle.SYSTEM_CORE, "access-script", "execute", "run", true, playerId);
    SystemRecoveryAlarm.activate();
    level.triggerSystemCoreWarningCall();
    TerminalInterpreter.instance().synchronizeState(TerminalStep.CENTRAL_SORT.stateId());
    return true;
  }

  /**
   * Returns a point validated during this level's first setup tick.
   *
   * @param name validated custom-point name
   * @return resolved world point
   */
  Point point(String name) {
    Point point = resolvedPoints.get(name);
    if (point == null) {
      throw new IllegalStateException("Validated System Recovery point is unavailable: " + name);
    }
    return point;
  }
}
