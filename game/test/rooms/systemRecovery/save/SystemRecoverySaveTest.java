package rooms.systemRecovery.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import feature.hints.HintSystem;
import feature.petrinet.PetriNetSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
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
                    "riddle1.tab", "systemRecovery.story.energy", 12, false, "System", false)));
    Path savePath = temporaryDirectory.resolve("system-recovery-save.json");

    SystemRecoverySave.write(savePath, expected);

    assertTrue(Files.isRegularFile(savePath));
    assertEquals(expected, SystemRecoveryLoad.read(savePath).orElseThrow());
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
    assertFalse(SystemRecoveryLoad.isMainPuzzleCheckpoint(SystemRecoveryLearningStep.ENERGY_VALUES));
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
    assertEquals(SystemRecoveryLearningStep.MODULE_ARRAY, SystemRecoveryProgressNet.activeStep().orElseThrow());
    assertEquals(2, TerminalInterpreter.instance().currentState());
    assertEquals(2, TerminalInterpreter.instance().acceptedInputs().size());
  }

  @Test
  void eachMainRiddleCheckpointRequiresItsOwnTerminalBoundary() throws Exception {
    for (SystemRecoveryLearningStep step : SystemRecoveryLearningStep.values()) {
      if (!SystemRecoveryLoad.isMainPuzzleCheckpoint(step)) continue;
      int acceptedCount = switch (step) {
        case ENERGY_ARRAY -> 0;
        case MODULE_ARRAY -> 2;
        case INVENTORY_COUNT -> 6;
        case TRANSPORT_ARRAY -> 7;
        case MANUAL_SORTING, BUBBLE_SORT_CONDITION, ARCHIVE_ACCESS -> 9;
        case STORAGE_ARRAY -> 10;
        case SEARCH_PROGRAM, SYSTEM_CORE_ACCESS -> 12;
        default -> throw new AssertionError(step);
      };
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
          acceptedCount == 0 ? List.of(new SystemRecoverySave.AcceptedInput(0, "bad"))
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
