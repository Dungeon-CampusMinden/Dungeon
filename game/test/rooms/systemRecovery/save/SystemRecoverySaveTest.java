package rooms.systemRecovery.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import feature.components.InventoryComponent;
import feature.hints.HintSystem;
import feature.inventory.items.ItemKey;
import feature.petrinet.PetriNetSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SortProgramStickItem;
import rooms.systemRecovery.modules.computer.MountedPuzzleItems;
import rooms.systemRecovery.modules.computer.UsbProgramDraft;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.util.SystemRecoveryAchievementTracker;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;
import rooms.systemRecovery.util.interpreter.TerminalStep;

/** Tests the checkpoint file contract independently from the rendered level. */
class SystemRecoverySaveTest {

  @TempDir Path temporaryDirectory;

  @BeforeEach
  void setUp() {
    MountedPuzzleItems.clear();
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new PetriNetSystem());
    Game.add(new HintSystem());
    SystemRecoveryProgressNet.initialize();
    TerminalInterpreter.instance().reset();
    TerminalInterpreterSetup.setupPreviewStates();
  }

  @AfterEach
  void tearDown() {
    MountedPuzzleItems.clear();
    SystemRecoveryProgressNet.reset();
    TerminalInterpreter.instance().reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  @Test
  void mountedChipIsSavedAsOwnedItemAndDoesNotDuplicateInventoryVersion() {
    SortProgramStickItem mountedStick =
        new SortProgramStickItem(false, "if (array[j] > array[j + 1]) {");
    Entity player = new Entity("mounted-stick-owner");
    player.add(new PlayerComponent(true, "Ada"));
    MountedPuzzleItems.mount(player, mountedStick);
    Game.add(player);

    List<SystemRecoverySave.PlayerItemData> onlyMounted =
        SystemRecoverySave.mergeMountedPuzzleItems(List.of(), MountedPuzzleItems.snapshot());
    List<SystemRecoverySave.PlayerItemData> inventoryPreferred =
        SystemRecoverySave.mergeMountedPuzzleItems(
            List.of(new SystemRecoverySave.PlayerItemData("Ada", "sort-program-stick", true)),
            MountedPuzzleItems.snapshot());
    SystemRecoverySave.SaveData duringMachineRun =
        SystemRecoverySave.capture(
            SystemRecoveryLearningStep.BUBBLE_SORT_MACHINE, UUID.randomUUID());

    assertEquals(1, onlyMounted.size());
    assertEquals("sort-program-stick", onlyMounted.getFirst().itemKey());
    assertEquals("if (array[j] > array[j + 1]) {", onlyMounted.getFirst().draft());
    assertEquals(1, inventoryPreferred.size());
    assertTrue(inventoryPreferred.getFirst().programmed());
    assertEquals(onlyMounted, duringMachineRun.inventoryItems());
    assertTrue(MountedPuzzleItems.unmount(player.id(), mountedStick));
    assertTrue(
        SystemRecoverySave.capture(SystemRecoveryLearningStep.ARCHIVE_ACCESS, UUID.randomUUID())
            .inventoryItems()
            .isEmpty());
  }

  @Test
  void writesAndReadsCheckpointWithTerminalHistory() throws Exception {
    List<SystemRecoverySave.AcceptedInput> inputs =
        List.of(
            new SystemRecoverySave.AcceptedInput(
                0, TerminalInterpreterSetup.debugSourceForState(0).orElseThrow()),
            new SystemRecoverySave.AcceptedInput(
                1, TerminalInterpreterSetup.debugSourceForState(1).orElseThrow()));
    SystemRecoverySave.SaveData expected =
        new SystemRecoverySave.SaveData(
            SystemRecoveryLearningStep.MODULE_ARRAY.hintKey(),
            inputs,
            List.of(
                new SystemRecoverySave.QuestLogEntryData(
                    "riddle1.tab", "systemRecovery.story.energy", 12, false, "System", false),
                new SystemRecoverySave.QuestLogEntryData(
                    "Notes", "remember the GPU", 42, true, "Ada", true)),
            new SystemRecoveryAchievementTracker.Snapshot(
                false,
                true,
                true,
                2,
                1,
                List.of("energy-array"),
                List.of("energy-array"),
                List.of("module-storage"),
                List.of("module-storage"),
                List.of("sort"),
                List.of("search"),
                List.of(SystemRecoveryAchievements.INTENTIONAL_FAILURE)));
    Path savePath = temporaryDirectory.resolve("system-recovery-save.json");

    SystemRecoverySave.write(savePath, expected);

    assertTrue(Files.isRegularFile(savePath));
    assertEquals(expected, SystemRecoveryLoad.read(savePath).orElseThrow());
  }

  @Test
  void preservesRunMetadata() throws Exception {
    UUID runId = UUID.randomUUID();
    SystemRecoverySave.SaveData expected =
        new SystemRecoverySave.SaveData(
            SystemRecoveryLearningStep.ENERGY_ARRAY.hintKey(),
            List.of(),
            List.of(),
            runId,
            "Ada",
            true,
            null);
    Path savePath = temporaryDirectory.resolve("run-id-save.json");

    SystemRecoverySave.write(savePath, expected);

    SystemRecoverySave.SaveData restored = SystemRecoveryLoad.read(savePath).orElseThrow();
    assertEquals(runId, restored.runId());
    assertEquals("Ada", restored.playerName());
    assertEquals(true, restored.trackingConsent());
  }

  @Test
  void persistsTerminalHistoryMemoryWatchAndProgrammedInventory() throws Exception {
    SystemRecoverySave.SaveData expected =
        new SystemRecoverySave.SaveData(
            SystemRecoveryLearningStep.SEARCH_ROBOT_RUN.hintKey(),
            terminalInputsThrough(12),
            List.of(),
            UUID.randomUUID(),
            "Ada",
            true,
            null,
            List.of("int[] map = new int[3][5];", "roboter.collect();"),
            List.of("map\tint[][]\t[[0, 1], [1, 0]]"),
            List.of(new SystemRecoverySave.PlayerItemData("Ada", "search-program-chip", true)),
            false);
    Path savePath = temporaryDirectory.resolve("complete-state.json");

    SystemRecoverySave.write(savePath, expected);

    assertEquals(expected, SystemRecoveryLoad.read(savePath).orElseThrow());
  }

  @Test
  void persistsNamedPlayerPositions() throws Exception {
    SystemRecoverySave.PlayerPositionData position =
        new SystemRecoverySave.PlayerPositionData("Ada", 12.5f, 3.5f);
    SystemRecoverySave.SaveData expected =
        saveForCheckpoint(SystemRecoveryLearningStep.ENERGY_ARRAY, List.of())
            .withPlayerPositions(List.of(position));
    Path savePath = temporaryDirectory.resolve("player-position.json");

    SystemRecoverySave.write(savePath, expected);

    assertEquals(
        List.of(position), SystemRecoveryLoad.read(savePath).orElseThrow().playerPositions());
  }

  @Test
  void restoresPositionToThePlayerWithTheMatchingNameAndKeepsUnjoinedPlayersPending() {
    Entity ada = new Entity("ada");
    ada.add(new PlayerComponent(true, "Ada"));
    PositionComponent position = new PositionComponent(new Point(1f, 1f));
    ada.add(position);
    List<SystemRecoverySave.PlayerPositionData> bobPosition =
        List.of(new SystemRecoverySave.PlayerPositionData("Bob", 8f, 9f));

    List<SystemRecoverySave.PlayerPositionData> pending =
        SystemRecoveryLoad.restorePlayerPositions(
            List.of(ada),
            List.of(
                new SystemRecoverySave.PlayerPositionData("Ada", 12.5f, 3.5f),
                bobPosition.getFirst()));

    assertEquals(new Point(12.5f, 3.5f), position.position());
    assertEquals(bobPosition, pending);
  }

  @Test
  void persistsAnUnprogrammedUsbDraftAcrossLoad() throws Exception {
    String draft = UsbProgramDraft.encode(List.of("map[row].length", "map[row][column] == 1", ""));
    SystemRecoverySave.SaveData expected =
        new SystemRecoverySave.SaveData(
            SystemRecoveryLearningStep.SEARCH_PROGRAM.hintKey(),
            terminalInputsThrough(12),
            List.of(),
            UUID.randomUUID(),
            "Ada",
            true,
            null,
            List.of(),
            List.of(),
            List.of(
                new SystemRecoverySave.PlayerItemData("Ada", "search-program-chip", false, draft)),
            false);
    Path savePath = temporaryDirectory.resolve("usb-draft.json");

    SystemRecoverySave.write(savePath, expected);

    assertEquals(expected, SystemRecoveryLoad.read(savePath).orElseThrow());
    assertEquals(9, SystemRecoverySave.FORMAT_VERSION);
  }

  @Test
  void persistsWhetherTheSystemCoreWarningCallWasAnswered() throws Exception {
    SystemRecoverySave.SaveData base =
        saveForCheckpoint(
            SystemRecoveryLearningStep.CORE_SORT,
            terminalInputsThrough(
                SystemRecoveryLearningStep.CORE_SORT.acceptedTerminalInputCount()));
    SystemRecoverySave.SaveData expected =
        new SystemRecoverySave.SaveData(
            base.checkpointKey(),
            base.acceptedTerminalInputs(),
            base.questLog(),
            base.runId(),
            base.playerName(),
            base.trackingConsent(),
            base.achievementProgress(),
            base.terminalHistory(),
            base.memoryWatchEntries(),
            base.inventoryItems(),
            false,
            true);
    Path savePath = temporaryDirectory.resolve("answered-core-call.json");

    SystemRecoverySave.write(savePath, expected);

    assertEquals(expected, SystemRecoveryLoad.read(savePath).orElseThrow());
    assertTrue(SystemRecoveryLoad.read(savePath).orElseThrow().systemCoreWarningCallAnswered());
  }

  @Test
  void capturesTheAuthoritativePlayerNameInsteadOfTheJvmFallback() {
    Entity player = new Entity("authoritative-player");
    player.add(new PlayerComponent(true, "Ada"));
    player.add(new PositionComponent(new Point(12.5f, 3.5f)));
    Game.add(player);

    SystemRecoverySave.SaveData save =
        SystemRecoverySave.capture(SystemRecoveryLearningStep.ENERGY_ARRAY, UUID.randomUUID());

    assertEquals("Ada", save.playerName());
    assertEquals(
        List.of(new SystemRecoverySave.PlayerPositionData("Ada", 12.5f, 3.5f)),
        save.playerPositions());
  }

  @Test
  void capturesProgrammedPuzzleItemsWithTheirOwner() {
    Entity player = new Entity("authoritative-player");
    player.add(new PlayerComponent(true, "Ada"));
    InventoryComponent inventory = new InventoryComponent(1);
    inventory.add(new SearchProgramChipItem(true));
    player.add(inventory);
    Game.add(player);

    SystemRecoverySave.SaveData save =
        SystemRecoverySave.capture(SystemRecoveryLearningStep.SEARCH_ROBOT_RUN, UUID.randomUUID());

    assertEquals(
        List.of(new SystemRecoverySave.PlayerItemData("Ada", "search-program-chip", true)),
        save.inventoryItems());
  }

  @Test
  void captureRetainsTheProgrammedChipOfAnAbsentPlayer() throws Exception {
    Entity host = new Entity("connected-host");
    host.add(new PlayerComponent(true, "Ada"));
    Game.add(host);
    SystemRecoverySave.PlayerItemData guestChip =
        new SystemRecoverySave.PlayerItemData("Bob", "search-program-chip", true);

    SystemRecoverySave.SaveData save =
        SystemRecoverySave.capture(
            SystemRecoveryLearningStep.ENERGY_ARRAY, UUID.randomUUID(), true, List.of(guestChip));
    Path savePath = temporaryDirectory.resolve("pending-guest-chip.json");
    SystemRecoverySave.write(savePath, save);

    assertEquals("Ada", save.playerName());
    assertEquals(List.of(guestChip), save.inventoryItems());
    assertEquals(
        List.of(guestChip), SystemRecoveryLoad.read(savePath).orElseThrow().inventoryItems());
  }

  @Test
  void uploadCheckpointRemainsLoadableWithOnlyTheGuestChipSaved() throws Exception {
    SystemRecoverySave.PlayerItemData guestChip =
        new SystemRecoverySave.PlayerItemData("Bob", "search-program-chip", true);
    SystemRecoverySave.SaveData save =
        saveForCheckpoint(SystemRecoveryLearningStep.SEARCH_ROBOT_RUN, terminalInputsThrough(12))
            .withInventoryItems(List.of(guestChip));
    Path savePath = temporaryDirectory.resolve("guest-upload-checkpoint.json");

    SystemRecoverySave.write(savePath, save);

    assertEquals(
        List.of(guestChip), SystemRecoveryLoad.read(savePath).orElseThrow().inventoryItems());
  }

  @Test
  void currentInventoryOverridesAnOutdatedPendingVersion() {
    Entity player = new Entity("connected-owner");
    player.add(new PlayerComponent(true, "Ada"));
    InventoryComponent inventory = new InventoryComponent(1);
    inventory.add(new SearchProgramChipItem(true));
    player.add(inventory);
    Game.add(player);

    SystemRecoverySave.SaveData save =
        SystemRecoverySave.capture(
            SystemRecoveryLearningStep.ENERGY_ARRAY,
            UUID.randomUUID(),
            true,
            List.of(new SystemRecoverySave.PlayerItemData("Ada", "search-program-chip", false)));

    assertEquals(
        List.of(new SystemRecoverySave.PlayerItemData("Ada", "search-program-chip", true)),
        save.inventoryItems());
  }

  @Test
  void capturesAnUnprogrammedChipDraftFromThePlayerInventory() {
    Entity player = new Entity("authoritative-player");
    player.add(new PlayerComponent(true, "Ada"));
    String draft = UsbProgramDraft.encode(List.of("map[row].length", "", ""));
    InventoryComponent inventory = new InventoryComponent(1);
    inventory.add(new SearchProgramChipItem(false, draft));
    player.add(inventory);
    Game.add(player);

    SystemRecoverySave.SaveData save =
        SystemRecoverySave.capture(SystemRecoveryLearningStep.SEARCH_PROGRAM, UUID.randomUUID());

    assertEquals(
        List.of(new SystemRecoverySave.PlayerItemData("Ada", "search-program-chip", false, draft)),
        save.inventoryItems());
  }

  @Test
  void capturesTheArchiveKeyForTheArchiveAccessCheckpoint() {
    Entity player = new Entity("authoritative-player");
    player.add(new PlayerComponent(true, "Ada"));
    InventoryComponent inventory = new InventoryComponent(1);
    inventory.add(new ItemKey());
    player.add(inventory);
    Game.add(player);

    SystemRecoverySave.SaveData save =
        SystemRecoverySave.capture(SystemRecoveryLearningStep.ARCHIVE_ACCESS, UUID.randomUUID());

    assertEquals(
        List.of(new SystemRecoverySave.PlayerItemData("Ada", "archive-key", false, "")),
        save.inventoryItems());
  }

  @Test
  void writesAndReadsTheArchiveKeyInTheCheckpointFile() throws Exception {
    SystemRecoverySave.SaveData save =
        new SystemRecoverySave.SaveData(
            SystemRecoveryLearningStep.ARCHIVE_ACCESS.hintKey(),
            terminalInputsThrough(9),
            List.of(),
            UUID.randomUUID(),
            "Ada",
            true,
            null,
            List.of(),
            List.of(),
            List.of(new SystemRecoverySave.PlayerItemData("Ada", "archive-key", false, "")),
            false);
    Path savePath = temporaryDirectory.resolve("archive-key-save.json");

    SystemRecoverySave.write(savePath, save);

    assertEquals(save, SystemRecoveryLoad.read(savePath).orElseThrow());
  }

  @Test
  void rejectsUnsupportedAndMalformedSaves() throws Exception {
    Path versionPath = temporaryDirectory.resolve("version.json");
    Files.writeString(versionPath, "{\"formatVersion\":99,\"checkpoint\":\"energy-array\"}");
    Path malformedPath = temporaryDirectory.resolve("malformed.json");
    Files.writeString(malformedPath, "not-json");

    assertTrue(SystemRecoveryLoad.read(versionPath).isEmpty());
    assertTrue(SystemRecoveryLoad.read(malformedPath).isEmpty());
    assertTrue(SystemRecoveryLoad.read(temporaryDirectory.resolve("missing.json")).isEmpty());
    assertFalse(
        SystemRecoveryLoad.isMainPuzzleCheckpoint(SystemRecoveryLearningStep.ENERGY_VALUES));
  }

  @Test
  void delaysNewAutomaticSavesUntilTheFirstRiddleIsComplete() {
    assertFalse(SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.ENERGY_ARRAY));
    assertTrue(SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.MODULE_ARRAY));
  }

  @Test
  void savesProgrammedChipsAndEverySystemCoreSubstep() {
    assertTrue(
        SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.BUBBLE_SORT_MACHINE));
    assertTrue(
        SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.SEARCH_ROBOT_RUN));
    assertTrue(
        SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS));
    assertTrue(SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.CORE_SORT));
    assertTrue(SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.CORE_COUNT));
    assertTrue(SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.CORE_SEARCH));
    assertTrue(
        SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.CORE_SEARCH_ROBOT));
    assertTrue(SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.CORE_META));
    assertTrue(SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.COMPLETE));
  }

  @Test
  void savesTheUnlockedArchiveDoorBeforeTheArchiveTerminalPuzzle() {
    assertTrue(
        SystemRecoveryLoad.isMainPuzzleCheckpoint(SystemRecoveryLearningStep.ARCHIVE_ARRAYS));
    assertTrue(SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.ARCHIVE_ARRAYS));
    assertEquals(9, SystemRecoveryLearningStep.ARCHIVE_ARRAYS.acceptedTerminalInputCount());
  }

  @Test
  void restoresPetriPlaceAndFlexibleTerminalContextWithoutCallbacks() {
    List<SystemRecoverySave.AcceptedInput> inputs =
        List.of(
            new SystemRecoverySave.AcceptedInput(
                0, TerminalInterpreterSetup.debugSourceForState(0).orElseThrow()),
            new SystemRecoverySave.AcceptedInput(
                1, TerminalInterpreterSetup.debugSourceForState(1).orElseThrow()));
    SystemRecoverySave.SaveData save =
        new SystemRecoverySave.SaveData(
            SystemRecoveryLearningStep.MODULE_ARRAY.hintKey(), inputs, List.of());

    assertEquals(
        SystemRecoveryLearningStep.MODULE_ARRAY,
        SystemRecoveryLoad.restoreRuntime(save).orElseThrow());
    assertEquals(
        SystemRecoveryLearningStep.MODULE_ARRAY,
        SystemRecoveryProgressNet.activeStep().orElseThrow());
    assertEquals(2, TerminalInterpreter.instance().currentState());
    assertEquals(2, TerminalInterpreter.instance().acceptedInputs().size());
  }

  @Test
  void restoresRunLocalAchievementProgressWithoutEmittingUnlocks() {
    SystemRecoveryAchievementTracker.Snapshot expected =
        new SystemRecoveryAchievementTracker.Snapshot(
            true,
            true,
            true,
            5,
            3,
            List.of("energy-array"),
            List.of("energy-array"),
            List.of("energy-array"),
            List.of("energy-array"),
            List.of("search"),
            List.of("sort"),
            List.of(SystemRecoveryAchievements.INTENTIONAL_FAILURE));
    SystemRecoveryAchievements.resetRun(false);
    SystemRecoverySave.SaveData save =
        new SystemRecoverySave.SaveData(
            SystemRecoveryLearningStep.MODULE_ARRAY.hintKey(), List.of(), List.of(), expected);

    SystemRecoveryAchievements.restore(save.achievementProgress());

    assertEquals(expected, SystemRecoveryAchievements.snapshot());
  }

  @Test
  void eachMainRiddleCheckpointRequiresItsOwnTerminalBoundary() throws Exception {
    for (SystemRecoveryLearningStep step : SystemRecoveryLearningStep.values()) {
      if (!SystemRecoveryLoad.isMainPuzzleCheckpoint(step)) continue;
      int acceptedCount = step.acceptedTerminalInputCount();
      assertTrue(acceptedCount >= 0, step.name());
      List<SystemRecoverySave.AcceptedInput> history = terminalInputsThrough(acceptedCount);
      Path path = temporaryDirectory.resolve(step.hintKey() + ".json");
      SystemRecoverySave.SaveData valid = saveForCheckpoint(step, history);

      SystemRecoverySave.write(path, valid);
      assertEquals(valid, SystemRecoveryLoad.read(path).orElseThrow(), step.name());
      assertEquals(step, SystemRecoveryLoad.restoreRuntime(valid).orElseThrow(), step.name());
      assertEquals(
          step.terminalState(), TerminalInterpreter.instance().currentState(), step.name());

      List<SystemRecoverySave.AcceptedInput> wrongHistory =
          acceptedCount == 0
              ? List.of(new SystemRecoverySave.AcceptedInput(0, "bad"))
              : history.subList(0, acceptedCount - 1);
      SystemRecoverySave.SaveData invalid =
          new SystemRecoverySave.SaveData(step.hintKey(), wrongHistory, List.of());
      SystemRecoverySave.write(path, invalid);
      assertTrue(SystemRecoveryLoad.read(path).isEmpty(), step.name());
      assertTrue(SystemRecoveryLoad.restoreRuntime(invalid).isEmpty(), step.name());
    }
  }

  @Test
  void rejectsSyntacticallyValidButIncorrectTerminalHistory() {
    SystemRecoverySave.SaveData save =
        new SystemRecoverySave.SaveData(
            SystemRecoveryLearningStep.MODULE_ARRAY.hintKey(),
            List.of(
                new SystemRecoverySave.AcceptedInput(0, "int[] wrong = new int[5];"),
                new SystemRecoverySave.AcceptedInput(1, "bad")),
            List.of());

    assertTrue(SystemRecoveryLoad.restoreRuntime(save).isEmpty());
    assertEquals(0, TerminalInterpreter.instance().currentState());
    assertTrue(TerminalInterpreter.instance().acceptedInputs().isEmpty());
  }

  @Test
  void rejectsCoreCheckpointThatSkipsAnOrdinaryTerminalInput() {
    List<SystemRecoverySave.AcceptedInput> invalid =
        java.util.stream.Stream.concat(
                terminalInputsThrough(12).stream(),
                java.util.stream.Stream.of(
                    new SystemRecoverySave.AcceptedInput(
                        14, TerminalInterpreterSetup.debugSourceForState(14).orElseThrow())))
            .toList();
    SystemRecoverySave.SaveData save =
        new SystemRecoverySave.SaveData(
            SystemRecoveryLearningStep.CORE_COUNT.hintKey(), invalid, List.of());
    Path path = temporaryDirectory.resolve("invalid-core-history.json");

    try {
      SystemRecoverySave.write(path, save);
    } catch (java.io.IOException exception) {
      throw new AssertionError(exception);
    }

    assertTrue(SystemRecoveryLoad.read(path).isEmpty());
  }

  private static List<SystemRecoverySave.AcceptedInput> terminalInputsThrough(int count) {
    return java.util.Arrays.stream(TerminalStep.values())
        .filter(step -> step.inputMode() == TerminalStep.InputMode.TERMINAL)
        .limit(count)
        .map(
            step ->
                new SystemRecoverySave.AcceptedInput(
                    step.stateId(),
                    TerminalInterpreterSetup.debugSourceForState(step.stateId()).orElseThrow()))
        .toList();
  }

  private static SystemRecoverySave.SaveData saveForCheckpoint(
      SystemRecoveryLearningStep checkpoint, List<SystemRecoverySave.AcceptedInput> history) {
    List<SystemRecoverySave.PlayerItemData> items =
        switch (checkpoint) {
          case BUBBLE_SORT_MACHINE ->
              List.of(new SystemRecoverySave.PlayerItemData(null, "sort-program-stick", true));
          case SEARCH_ROBOT_RUN ->
              List.of(new SystemRecoverySave.PlayerItemData(null, "search-program-chip", true));
          default -> List.of();
        };
    return new SystemRecoverySave.SaveData(
        checkpoint.hintKey(),
        history,
        List.of(),
        UUID.randomUUID(),
        null,
        null,
        null,
        List.of(),
        List.of(),
        items,
        false);
  }
}
