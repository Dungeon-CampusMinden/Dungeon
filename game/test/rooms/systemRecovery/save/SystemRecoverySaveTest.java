package rooms.systemRecovery.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import engine.Entity;
import engine.components.PlayerComponent;
import feature.hints.HintSystem;
import feature.petrinet.PetriNetSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.util.SystemRecoveryAchievementTracker;
import rooms.systemRecovery.util.SystemRecoveryAchievements;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/** Tests the checkpoint file contract independently from the rendered level. */
class SystemRecoverySaveTest {

  @TempDir Path temporaryDirectory;

  @BeforeEach
  void setUp() {
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
    SystemRecoveryProgressNet.reset();
    TerminalInterpreter.instance().reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
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
  void capturesTheAuthoritativePlayerNameInsteadOfTheJvmFallback() {
    Entity player = new Entity("authoritative-player");
    player.add(new PlayerComponent(true, "Ada"));
    Game.add(player);

    SystemRecoverySave.SaveData save =
        SystemRecoverySave.capture(SystemRecoveryLearningStep.ENERGY_ARRAY, UUID.randomUUID());

    assertEquals("Ada", save.playerName());
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
    assertFalse(
        SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.ENERGY_ARRAY));
    assertTrue(
        SystemRecoveryLoad.isAutoSaveCheckpoint(SystemRecoveryLearningStep.MODULE_ARRAY));
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
      List<SystemRecoverySave.AcceptedInput> history =
          IntStream.range(0, acceptedCount)
              .mapToObj(
                  state ->
                      new SystemRecoverySave.AcceptedInput(
                          state, TerminalInterpreterSetup.debugSourceForState(state).orElseThrow()))
              .toList();
      Path path = temporaryDirectory.resolve(step.hintKey() + ".json");
      SystemRecoverySave.SaveData valid =
          new SystemRecoverySave.SaveData(step.hintKey(), history, List.of());

      SystemRecoverySave.write(path, valid);
      assertEquals(valid, SystemRecoveryLoad.read(path).orElseThrow(), step.name());
      assertEquals(step, SystemRecoveryLoad.restoreRuntime(valid).orElseThrow(), step.name());
      assertEquals(acceptedCount, TerminalInterpreter.instance().currentState(), step.name());

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
}
