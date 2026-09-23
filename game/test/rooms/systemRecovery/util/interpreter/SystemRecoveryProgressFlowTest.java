package rooms.systemRecovery.util.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import feature.hints.Hint;
import feature.hints.HintSystem;
import feature.petrinet.PetriNetSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.ProgressDebugSnapshot;
import rooms.systemRecovery.petrinet.SystemRecoveryHintCatalog;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents;

/**
 * Integration coverage for registered terminal requirements and the shared learning progression.
 */
class SystemRecoveryProgressFlowTest {

  private final TerminalInterpreter interpreter = TerminalInterpreter.instance();
  private final List<TerminalAttempt> unavailableAttempts = new ArrayList<>();
  private HintSystem hints;
  private SystemRecoveryTerminalController terminalController;

  @BeforeEach
  void setUp() {
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new PetriNetSystem());
    hints = new HintSystem();
    Game.add(hints);
    SystemRecoveryProgressNet.initialize();
    interpreter.reset();
    TerminalInterpreterSetup.setupRoomStates();
    unavailableAttempts.clear();
    terminalController =
        new SystemRecoveryTerminalController(
            interpreter, SystemRecoveryProgressNet::activeStep, unavailableAttempts::add);
  }

  @AfterEach
  void tearDown() {
    interpreter.reset();
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  @Test
  void completesEveryLearningStepThroughItsRegisteredSuccessRoute() {
    assertState(SystemRecoveryLearningStep.ENERGY_ARRAY, 0);
    assertFalse(interpreter.interpret(moduleArray(), 101), "future terminal code must be rejected");
    assertFalse(SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.MODULE_ARRAY));
    assertState(SystemRecoveryLearningStep.ENERGY_ARRAY, 0);

    submit(energyArray(), 101, SystemRecoveryLearningStep.ENERGY_VALUES, 1);
    assertFalse(interpreter.interpret(energyArray(), 202), "a completed chunk cannot repeat");
    assertState(SystemRecoveryLearningStep.ENERGY_VALUES, 1);
    submit(energyValues(), 202, SystemRecoveryLearningStep.ENERGY_INSERT_BATTERY, 2);

    complete(
        SystemRecoveryLearningStep.ENERGY_INSERT_BATTERY,
        101,
        SystemRecoveryLearningStep.MODULE_ARRAY,
        2);
    submit(moduleArray(), 202, SystemRecoveryLearningStep.MODULE_VALUES, 3);
    submit(moduleValues(), 101, SystemRecoveryLearningStep.MODULE_REMOVE_GPU, 4);
    submit(removeGpu(), 202, SystemRecoveryLearningStep.MODULE_LENGTH, 5);
    submit(moduleLength(), 101, SystemRecoveryLearningStep.ROOM2_DOOR_CODE, 6);
    complete(
        SystemRecoveryLearningStep.ROOM2_DOOR_CODE,
        202,
        SystemRecoveryLearningStep.INVENTORY_COUNT,
        6);
    submit(moduleCount(), 101, SystemRecoveryLearningStep.ROOM3_DOOR_CODE, 7);
    complete(
        SystemRecoveryLearningStep.ROOM3_DOOR_CODE,
        202,
        SystemRecoveryLearningStep.TRANSPORT_ARRAY,
        7);
    submit(packageArray(), 101, SystemRecoveryLearningStep.TRANSPORT_COLLECT_LOOP, 8);
    submit(packageLoop(), 202, SystemRecoveryLearningStep.DATA_STORAGE_DOOR_OPEN, 9);
    complete(
        SystemRecoveryLearningStep.DATA_STORAGE_DOOR_OPEN,
        101,
        SystemRecoveryLearningStep.MANUAL_SORTING,
        9);

    completeWithRiddleCallback(
        SystemRecoveryLearningStep.MANUAL_SORTING,
        SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION,
        SystemRecoveryPuzzle.MANUAL_SORTING,
        "sort-display",
        "choice",
        202);
    complete(
        SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION,
        101,
        SystemRecoveryLearningStep.BUBBLE_SORT_MACHINE,
        9);
    completeWithRiddleCallback(
        SystemRecoveryLearningStep.BUBBLE_SORT_MACHINE,
        SystemRecoveryLearningStep.ARCHIVE_ACCESS,
        SystemRecoveryPuzzle.BUBBLE_SORT,
        "sort-machine",
        "run",
        202);
    complete(
        SystemRecoveryLearningStep.ARCHIVE_ACCESS,
        101,
        SystemRecoveryLearningStep.ARCHIVE_ARRAYS,
        9);

    submit(archiveArrays(), 202, SystemRecoveryLearningStep.STORAGE_ARRAY, 10);
    submit(storageArray(), 101, SystemRecoveryLearningStep.STORAGE_VALUES, 11);
    submit(storageValues(), 202, SystemRecoveryLearningStep.SEARCH_PROGRAM, 12);
    assertTrue(TerminalInterpreterSetup.matchesSearchRobotProgram(mapSearchLoop()));
    complete(
        SystemRecoveryLearningStep.SEARCH_PROGRAM,
        101,
        SystemRecoveryLearningStep.SEARCH_ROBOT_RUN,
        12);
    completeWithRiddleCallback(
        SystemRecoveryLearningStep.SEARCH_ROBOT_RUN,
        SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS,
        SystemRecoveryPuzzle.SEARCH_ROBOT,
        "search-controller",
        "chip",
        202);
    complete(
        SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS,
        101,
        SystemRecoveryLearningStep.CORE_SORT,
        12);
    interpreter.synchronizeState(13);
    assertState(SystemRecoveryLearningStep.CORE_SORT, 13);

    submit(bubbleSort(), 202, SystemRecoveryLearningStep.CORE_COUNT, 14);
    submit(centralModuleCount(), 101, SystemRecoveryLearningStep.CORE_SEARCH, 15);
    submit(centralMapSearch(), 202, SystemRecoveryLearningStep.CORE_SEARCH_ROBOT, 16);
    complete(
        SystemRecoveryLearningStep.CORE_SEARCH_ROBOT,
        202,
        SystemRecoveryLearningStep.CORE_META,
        16);
    interpreter.synchronizeState(17);
    complete(SystemRecoveryLearningStep.CORE_META, 101, SystemRecoveryLearningStep.COMPLETE, 17);
    assertNoActiveHint();
  }

  @Test
  void moduleArrayIsRejectedUntilBatteryInsertionActivatesItsPlace() {
    submit(energyArray(), 101, SystemRecoveryLearningStep.ENERGY_VALUES, 1);
    submit(energyValues(), 101, SystemRecoveryLearningStep.ENERGY_INSERT_BATTERY, 2);

    assertFalse(terminalController.interpret(moduleArray(), 202));
    assertState(SystemRecoveryLearningStep.ENERGY_INSERT_BATTERY, 2);
    assertEquals(1, unavailableAttempts.size());
    assertEquals(2, unavailableAttempts.getFirst().state());
    assertEquals(moduleArray(), unavailableAttempts.getFirst().source());

    complete(
        SystemRecoveryLearningStep.ENERGY_INSERT_BATTERY,
        101,
        SystemRecoveryLearningStep.MODULE_ARRAY,
        2);
    submit(moduleArray(), 202, SystemRecoveryLearningStep.MODULE_VALUES, 3);
  }

  private void submit(
      String source, int playerId, SystemRecoveryLearningStep nextStep, int nextTerminalState) {
    SystemRecoveryLearningStep current = SystemRecoveryProgressNet.activeStep().orElseThrow();
    assertEquals(current.terminalStep().orElseThrow().stateId(), interpreter.currentState());
    assertTrue(terminalController.interpret(source, playerId), "source was rejected:\n" + source);
    assertState(nextStep, nextTerminalState);
  }

  private void complete(
      SystemRecoveryLearningStep current,
      int playerId,
      SystemRecoveryLearningStep next,
      int terminalState) {
    assertEquals(current, SystemRecoveryProgressNet.activeStep().orElseThrow());
    SystemRecoveryPuzzleEvents.attempt(
        current.puzzle().orElse(SystemRecoveryPuzzle.ENERGY),
        "integration-flow",
        "success-source",
        "accepted",
        true,
        playerId);
    assertTrue(SystemRecoveryProgressNet.complete(current), "could not complete " + current);
    assertState(next, terminalState);
  }

  private void completeWithRiddleCallback(
      SystemRecoveryLearningStep current,
      SystemRecoveryLearningStep next,
      SystemRecoveryPuzzle puzzle,
      String objectId,
      String answerKind,
      int playerId) {
    assertEquals(current, SystemRecoveryProgressNet.activeStep().orElseThrow());
    RiddleCallbacks callbacks =
        SystemRecoveryPuzzleEvents.forPuzzleCompleting(puzzle, objectId, answerKind, current);
    callbacks.success("intermediate-action", playerId);
    assertState(current, interpreter.currentState());
    callbacks.solved();
    callbacks.solved();
    assertState(next, interpreter.currentState());
  }

  private void assertState(SystemRecoveryLearningStep expected, int terminalState) {
    assertEquals(expected, SystemRecoveryProgressNet.activeStep().orElseThrow());
    assertEquals(terminalState, interpreter.currentState());
    ProgressDebugSnapshot snapshot = SystemRecoveryProgressNet.debugSnapshot();
    assertTrue(snapshot.consistent());
    assertEquals(1, snapshot.totalTokens());
    assertEquals(1, snapshot.tokenCounts().values().stream().mapToInt(Integer::intValue).sum());
    if (expected.isLearningStep()) {
      Hint[] expectedHints = SystemRecoveryHintCatalog.hints(expected);
      assertEquals(expectedHints[0], hints.peekSharedHint().orElseThrow());
      assertNotNull(snapshot.activeStepKey());
    } else {
      assertNoActiveHint();
    }
  }

  private void assertNoActiveHint() {
    assertEquals(
        SystemRecoveryLearningStep.COMPLETE, SystemRecoveryProgressNet.activeStep().orElseThrow());
    assertTrue(hints.peekSharedHint().isEmpty());
    assertEquals(
        1,
        Arrays.stream(SystemRecoveryLearningStep.values())
            .mapToInt(
                step ->
                    SystemRecoveryProgressNet.debugSnapshot()
                        .tokenCounts()
                        .getOrDefault(step.hintKey(), 0))
            .sum());
  }

  private static String energyArray() {
    return "int[] energie = new int[5];";
  }

  private static String energyValues() {
    return "energie[0]=40; energie[1]=10; energie[2]=80; energie[3]=30; energie[4]=60;";
  }

  private static String moduleArray() {
    return "String[] module = new String[5];";
  }

  private static String moduleValues() {
    return "module[0]=\"CPU\"; module[1]=\"RAM\"; module[2]=\"GPU\"; module[3]=\"SSD\"; module[4]=\"NETWORK\";";
  }

  private static String removeGpu() {
    return "module[2] = null;";
  }

  private static String moduleLength() {
    return "module.length;";
  }

  private static String moduleCount() {
    return "int count=0; for (String item : module) { if (item != null) { count++; } }";
  }

  private static String packageArray() {
    return "int[] pakete = {15,40,20,60,30};";
  }

  private static String packageLoop() {
    return "for (int index=0; index<pakete.length; index++) { roboter.collect(); }";
  }

  private static String archiveArrays() {
    return "int[] energie={20,50,80}; String[] module={\"CPU\",\"GPU\",\"RAM\"}; boolean[] aktiv={true,false,true};";
  }

  private static String storageArray() {
    return "int[][] lager = new int[3][4];";
  }

  private static String storageValues() {
    return "lager[0][2]=1; lager[1][3]=2; lager[2][1]=3;";
  }

  private static String mapSearchLoop() {
    return "for (int row=0; row<map.length; row++) { for (int column=0; column<map[row].length; column++) { if (map[row][column] == 1) { roboter.collect(); } } }";
  }

  private static String bubbleSort() {
    return "for (int i=0; i<array.length-1; i++) { for (int j=0; j<array.length-1-i; j++) { if (array[j] > array[j+1]) { int temp=array[j]; array[j]=array[j+1]; array[j+1]=temp; } } }";
  }

  private static String centralModuleCount() {
    return "int count=0; for (String module : modules) { if (module != null) { count++; } }";
  }

  private static String centralMapSearch() {
    return "for (int row=0; row<map.length; row++) { for (int column=0; column<map[row].length; column++) { roboter.collect(); } }";
  }
}
