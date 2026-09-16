package rooms.systemRecovery.util.interpreter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import rooms.systemRecovery.modules.interpreter.CodeLine;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.modules.interpreter.TerminalCodeRequirement;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Sets up the interpreter states used by the System Recovery terminal puzzle. */
public final class TerminalInterpreterSetup {

  private static final String IDENTIFIER = "[a-zA-Z][a-zA-Z0-9]*";
  private static final Consumer<TerminalAttempt> SUCCESS = ignored -> {};
  private static final Consumer<TerminalAttempt> FAILURE =
      InterpretationCallbacks::onIncorrectTerminalInput;
  private static final String MODULE_ARRAY = "moduleArray";
  private static final String STORAGE_ARRAY = "storageArray";

  /** Riddle 1, step 1: create {@code int[] energie} with five slots. */
  private static final TerminalStep RIDDLE_ONE_STEP_ONE = TerminalStep.ENERGY_ARRAY;

  /** Riddle 1, step 2: assign the five required energy values to {@code energie}. */
  private static final TerminalStep RIDDLE_ONE_STEP_TWO = TerminalStep.ENERGY_VALUES;

  /** Riddle 2, step 1: create a five-slot {@code String[]} named {@code module}. */
  private static final TerminalStep RIDDLE_TWO_STEP_ONE = TerminalStep.MODULE_ARRAY;

  /** Riddle 2, step 2: assign CPU, RAM, GPU, SSD and NETWORK to the module slots. */
  private static final TerminalStep RIDDLE_TWO_STEP_TWO = TerminalStep.MODULE_VALUES;

  /** Riddle 2, step 3: remove the defective GPU by assigning {@code null} to slot 2. */
  private static final TerminalStep RIDDLE_TWO_STEP_THREE = TerminalStep.MODULE_REMOVE_GPU;

  /** Riddle 2, step 4: read {@code module.length} to reveal the array size. */
  private static final TerminalStep RIDDLE_TWO_STEP_FOUR = TerminalStep.MODULE_LENGTH;

  /** Riddle 3, step 1: count all non-null module entries with an enhanced {@code for} loop. */
  private static final TerminalStep RIDDLE_THREE_STEP_ONE = TerminalStep.INVENTORY_COUNT;

  /** Riddle 4, step 1: create {@code int[] pakete} with the five package weights. */
  private static final TerminalStep RIDDLE_FOUR_STEP_ONE = TerminalStep.TRANSPORT_ARRAY;

  /**
   * Riddle 4, step 2: iterate over {@code pakete} and call {@code roboter.collect(...)} once per
   * package.
   */
  private static final TerminalStep RIDDLE_FOUR_STEP_TWO = TerminalStep.TRANSPORT_COLLECT;

  /** Riddle 7, step 1: create the three arrays described in the data archive. */
  private static final TerminalStep RIDDLE_SEVEN_STEP_ONE = TerminalStep.ARCHIVE_ARRAYS;

  /** Riddle 8, step 1: create the three-by-four two-dimensional array {@code lager}. */
  private static final TerminalStep RIDDLE_EIGHT_STEP_ONE = TerminalStep.STORAGE_ARRAY;

  /** Riddle 8, step 2: assign the three required values to the specified {@code lager} cells. */
  private static final TerminalStep RIDDLE_EIGHT_STEP_TWO = TerminalStep.STORAGE_VALUES;

  /**
   * Riddle 9, step 1: replace the prepared {@code int j = 0} line with a nested loop that scans
   * every cell of {@code map} and calls {@code roboter.collect()} for every 1.
   */
  public static final int SEARCH_ROBOT_PROGRAM_STATE = TerminalStep.SEARCH_PROGRAM.stateId();

  private static final TerminalCodeRequirement SEARCH_ROBOT_PROGRAM_REQUIREMENT =
      createSearchRobotProgramRequirement();

  /**
   * Riddle 10, step 1: implement the Bubble Sort loop and swap adjacent values when the left value
   * is greater.
   */
  public static final int CENTRAL_SORT_STATE = TerminalStep.CENTRAL_SORT.stateId();

  /** Alias for the first terminal step of riddle 10. */
  private static final TerminalStep RIDDLE_TEN_STEP_ONE = TerminalStep.CENTRAL_SORT;

  /** Riddle 10, step 2: count all non-null entries in {@code modules}. */
  private static final TerminalStep RIDDLE_TEN_STEP_TWO = TerminalStep.CENTRAL_COUNT;

  /** Riddle 10, step 3: scan the two-dimensional {@code map} and collect every battery marker. */
  private static final TerminalStep RIDDLE_TEN_STEP_THREE = TerminalStep.CENTRAL_SEARCH;

  /** Riddle 10, meta state: reserved for the dedicated system-core input mask. */
  public static final int CENTRAL_META_STATE = TerminalStep.SYSTEM_CORE_META.stateId();

  public static final String ENERGIE_VALUE_0 = "40";
  public static final String ENERGIE_VALUE_1 = "10";
  public static final String ENERGIE_VALUE_2 = "80";
  public static final String ENERGIE_VALUE_3 = "30";
  public static final String ENERGIE_VALUE_4 = "60";

  private TerminalInterpreterSetup() {}

  /**
   * Sets up no-op callbacks so the client can preview interpretation feedback.
   *
   * <p>This is for headless unit testing
   */
  public static void setupPreviewStates() {
    setupAllTerminalRiddles(null, null);
  }

  /** Registers all terminal riddles in their gameplay order. */
  public static void setupRoomStates() {
    setupAllTerminalRiddles(SUCCESS, FAILURE);
  }

  private static void setupAllTerminalRiddles(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    setupRiddleOneMaterializationChamber(onSuccess, onFailure);
    setupRiddleTwoDefectiveModuleStorage(onSuccess, onFailure);
    setupRiddleThreeInventoryScanner(onSuccess, onFailure);
    setupRiddleFourTransportStorage(onSuccess, onFailure);
    setupRiddleFiveChaoticDataStorage();
    setupRiddleSixBubbleSortMachine();
    setupRiddleSevenDataArchive(onSuccess, onFailure);
    setupRiddleEightTwoDimensionalStorage(onSuccess, onFailure);
    setupRiddleTenCentralDataCenter(onSuccess, onFailure);
  }

  private static void setupRiddleOneMaterializationChamber(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    setupRiddleOneStepOneInitializeEnergyArray(
        successOrPreview(
            onSuccess, InterpretationCallbacks::onRiddleOneStepOneEnergyArrayInitialized),
        onFailure);
    setupRiddleOneStepTwoSetEnergyValues(
        successOrPreview(onSuccess, InterpretationCallbacks::onRiddleOneStepTwoEnergyValuesSet),
        onFailure);
  }

  private static void setupRiddleOneStepOneInitializeEnergyArray(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_ONE_STEP_ONE, unordered(onSuccess, onFailure, arrayCreation("int", "energie", 5)));
  }

  private static void setupRiddleOneStepTwoSetEnergyValues(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_ONE_STEP_TWO,
        unordered(
            onSuccess,
            onFailure,
            assignment("energie", 0, ENERGIE_VALUE_0),
            assignment("energie", 1, ENERGIE_VALUE_1),
            assignment("energie", 2, ENERGIE_VALUE_2),
            assignment("energie", 3, ENERGIE_VALUE_3),
            assignment("energie", 4, ENERGIE_VALUE_4)));
  }

  private static void setupRiddleTwoDefectiveModuleStorage(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    setupRiddleTwoStepOneInitializeModuleArray(
        successOrPreview(
            onSuccess, InterpretationCallbacks::onRiddleTwoStepOneModuleArrayInitialized),
        onFailure);
    setupRiddleTwoStepTwoSetModules(
        successOrPreview(onSuccess, InterpretationCallbacks::onRiddleTwoStepTwoModulesAssigned),
        onFailure);
    setupRiddleTwoStepThreeRemoveGpuModule(
        successOrPreview(onSuccess, InterpretationCallbacks::onRiddleTwoStepThreeGpuRemoved),
        onFailure);
    setupRiddleTwoStepFourReadModuleLength(
        successOrPreview(onSuccess, InterpretationCallbacks::onRiddleTwoStepFourModuleLengthRead),
        onFailure);
  }

  private static void setupRiddleTwoStepOneInitializeModuleArray(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_TWO_STEP_ONE,
        unordered(onSuccess, onFailure, arrayCreation("String", MODULE_ARRAY, 5, true)));
  }

  private static void setupRiddleTwoStepTwoSetModules(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_TWO_STEP_TWO,
        unordered(
            onSuccess,
            onFailure,
            capturedAssignment(MODULE_ARRAY, 0, "\"CPU\""),
            capturedAssignment(MODULE_ARRAY, 1, "\"RAM\""),
            capturedAssignment(MODULE_ARRAY, 2, "\"GPU\""),
            capturedAssignment(MODULE_ARRAY, 3, "\"SSD\""),
            capturedAssignment(MODULE_ARRAY, 4, "\"NETWORK\"")));
  }

  private static void setupRiddleTwoStepThreeRemoveGpuModule(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_TWO_STEP_THREE,
        unordered(onSuccess, onFailure, capturedAssignment(MODULE_ARRAY, 2, "null")));
  }

  private static void setupRiddleTwoStepFourReadModuleLength(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_TWO_STEP_FOUR, unordered(onSuccess, onFailure, capturedLengthAccess(MODULE_ARRAY)));
  }

  private static void setupRiddleThreeInventoryScanner(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_THREE_STEP_ONE,
        ordered(
            successOrPreview(
                onSuccess, InterpretationCallbacks::onRiddleThreeStepOneInventoryScannerCompleted),
            onFailure,
            declaration("int", "count", "0"),
            enhancedForLoop("String", capturedIdentifier(MODULE_ARRAY), "riddleThreeModuleItem"),
            notNullCondition("riddleThreeModuleItem"),
            increment("count")));
  }

  private static void setupRiddleFourTransportStorage(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    setupRiddleFourStepOneCreatePackages(
        successOrPreview(onSuccess, InterpretationCallbacks::onRiddleFourStepOnePackagesCreated),
        onFailure);
    setupRiddleFourStepTwoTransportPackages(
        successOrPreview(onSuccess, InterpretationCallbacks::onRiddleFourStepTwoPackagesCollected),
        onFailure);
  }

  private static void setupRiddleFourStepOneCreatePackages(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_FOUR_STEP_ONE,
        unordered(onSuccess, onFailure, intArrayLiteral("pakete", "15", "40", "20", "60", "30")));
  }

  private static void setupRiddleFourStepTwoTransportPackages(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_FOUR_STEP_TWO,
        ordered(
            onSuccess,
            onFailure,
            indexedForLoop("pakete", "packageIndex"),
            methodCallWithArrayAccess("roboter", "collect", "pakete", "packageIndex")));
  }

  private static void setupRiddleFiveChaoticDataStorage() {
    // Riddle 5 currently has no terminal input requirement.
  }

  private static void setupRiddleSixBubbleSortMachine() {
    // Riddle 6 currently has no terminal input requirement.
  }

  private static void setupRiddleSevenDataArchive(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_SEVEN_STEP_ONE,
        unordered(
            successOrPreview(
                onSuccess, InterpretationCallbacks::onRiddleSevenStepOneDataArchiveLoaded),
            onFailure,
            unorderedArrayLiteral("int", "energie", "20", "50", "80"),
            unorderedArrayLiteral("String", "module", "\"CPU\"", "\"GPU\"", "\"RAM\""),
            unorderedArrayLiteral("boolean", "aktiv", "true", "false", "true")));
  }

  private static void setupRiddleEightTwoDimensionalStorage(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    setupRiddleEightStepOneCreateStorage(
        successOrPreview(onSuccess, InterpretationCallbacks::onRiddleEightStepOneStorageCreated),
        onFailure);
    setupRiddleEightStepTwoFillStorage(
        successOrPreview(onSuccess, InterpretationCallbacks::onRiddleEightStepTwoStorageFilled),
        onFailure);
  }

  private static void setupRiddleEightStepOneCreateStorage(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_EIGHT_STEP_ONE,
        unordered(
            onSuccess, onFailure, twoDimensionalArrayCreation("int", STORAGE_ARRAY, 3, 4, true)));
  }

  private static void setupRiddleEightStepTwoFillStorage(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_EIGHT_STEP_TWO,
        unordered(
            onSuccess,
            onFailure,
            capturedTwoDimensionalAssignment(STORAGE_ARRAY, 0, 2, "1"),
            capturedTwoDimensionalAssignment(STORAGE_ARRAY, 1, 3, "2"),
            capturedTwoDimensionalAssignment(STORAGE_ARRAY, 2, 1, "3")));
  }

  /**
   * Creates the standalone requirement for the code stored on the search chip.
   *
   * <p>It is intentionally not registered in the shared terminal state map. State 12 only marks
   * that the room is waiting for the chip editor; the normal terminal send button must not be able
   * to complete this step.
   *
   * @return immutable search-chip code requirement
   */
  public static TerminalCodeRequirement searchRobotProgramRequirement() {
    return SEARCH_ROBOT_PROGRAM_REQUIREMENT;
  }

  /**
   * Validates the source stored on the search chip against the registered search requirement.
   *
   * @param source source code from the chip editor
   * @return whether the source contains the completed nested row/column traversal and {@code
   *     collect()}
   */
  public static boolean matchesSearchRobotProgram(String source) {
    return TerminalInterpreter.instance().analyze(searchRobotProgramRequirement(), source);
  }

  private static TerminalCodeRequirement createSearchRobotProgramRequirement() {
    return ordered(
        null,
        null,
        outerTwoDimensionalLoop("map", "riddleNineMapRow"),
        innerTwoDimensionalLoop("map", "riddleNineMapRow", "riddleNineMapColumn"),
        twoDimensionalEqualsCondition("map", "riddleNineMapRow", "riddleNineMapColumn", "1"),
        methodCall("roboter", "collect"));
  }

  private static void setupRiddleTenCentralDataCenter(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    setupRiddleTenStepOneBubbleSort(
        successOrPreview(onSuccess, InterpretationCallbacks::onRiddleTenStepOneBubbleSortCompleted),
        onFailure);
    setupRiddleTenStepTwoCountModules(
        successOrPreview(onSuccess, InterpretationCallbacks::onRiddleTenStepTwoModulesCounted),
        onFailure);
    setupRiddleTenStepThreeFindBatteries(
        successOrPreview(
            onSuccess, InterpretationCallbacks::onRiddleTenStepThreeBatteriesCollected),
        onFailure);
  }

  private static void setupRiddleTenStepOneBubbleSort(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_TEN_STEP_ONE,
        ordered(
            onSuccess,
            onFailure,
            bubbleSortOuterLoop("sortArray", "outerIndex"),
            bubbleSortInnerLoop("sortArray", "outerIndex", "innerIndex"),
            arrayGreaterThanNextCondition("sortArray", "innerIndex"),
            declarationWithCapturedVariable(
                "int", "tempVariable", arrayAccess("sortArray", "innerIndex")),
            assignment(
                arrayAccess("sortArray", "innerIndex"),
                nextArrayAccessReference("sortArray", "innerIndex")),
            assignment(
                nextArrayAccess("sortArray", "innerIndex"), capturedIdentifier("tempVariable"))));
  }

  private static void setupRiddleTenStepTwoCountModules(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_TEN_STEP_TWO,
        ordered(
            successOrPreview(onSuccess, InterpretationCallbacks::onRiddleTenStepTwoModulesCounted),
            onFailure,
            declaration("int", "count", "0"),
            enhancedForLoop("String", "modules", "riddleTenModuleItem"),
            notNullCondition("riddleTenModuleItem"),
            increment("count")));
  }

  private static void setupRiddleTenStepThreeFindBatteries(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure) {
    register(
        RIDDLE_TEN_STEP_THREE,
        ordered(
            successOrPreview(
                onSuccess, InterpretationCallbacks::onRiddleTenStepThreeBatteriesCollected),
            onFailure,
            outerTwoDimensionalLoop("map", "riddleTenMapRow"),
            innerTwoDimensionalLoop("map", "riddleTenMapRow", "riddleTenMapColumn"),
            twoDimensionalEqualsCondition("map", "riddleTenMapRow", "riddleTenMapColumn", "1"),
            methodCall("roboter", "collect")));
  }

  private static TerminalCodeRequirement unordered(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure,
      CodeLine... codeLines) {
    return new TerminalCodeRequirement(codeLines, false, onSuccess, onFailure);
  }

  private static TerminalCodeRequirement ordered(
      java.util.function.Consumer<TerminalAttempt> onSuccess,
      java.util.function.Consumer<TerminalAttempt> onFailure,
      CodeLine... codeLines) {
    return new TerminalCodeRequirement(codeLines, true, onSuccess, onFailure);
  }

  private static void register(TerminalStep step, TerminalCodeRequirement requirement) {
    TerminalInterpreter.instance().register(step.stateId(), requirement);
  }

  private static java.util.function.Consumer<TerminalAttempt> successOrPreview(
      java.util.function.Consumer<TerminalAttempt> previewMarker,
      java.util.function.Consumer<TerminalAttempt> roomCallback) {
    return previewMarker == null ? null : roomCallback;
  }

  private static CodeLine arrayCreation(String type, String variable, int size) {
    return arrayCreation(type, variable, size, false);
  }

  private static CodeLine arrayCreation(String type, String variable, int size, boolean captured) {
    String variablePattern = captured ? capture(variable) : variable;
    return new CodeLine(
        Pattern.compile(
            type
                + "\\s*\\[\\s*]\\s*"
                + variablePattern
                + "\\s*=\\s*new\\s+"
                + type
                + "\\s*\\[\\s*"
                + size
                + "\\s*]"),
        Pattern.compile(
            type
                + "\\s+"
                + variablePattern
                + "\\s*\\[\\s*]\\s*=\\s*new\\s+"
                + type
                + "\\s*\\[\\s*"
                + size
                + "\\s*]"));
  }

  private static CodeLine twoDimensionalArrayCreation(
      String type, String variable, int rows, int columns) {
    return twoDimensionalArrayCreation(type, variable, rows, columns, false);
  }

  private static CodeLine twoDimensionalArrayCreation(
      String type, String variable, int rows, int columns, boolean captured) {
    String variablePattern = captured ? capture(variable) : variable;
    return new CodeLine(
        Pattern.compile(
            type
                + "\\s*\\[\\s*]\\s*\\[\\s*]\\s*"
                + variablePattern
                + "\\s*=\\s*new\\s+"
                + type
                + "\\s*\\[\\s*"
                + rows
                + "\\s*]\\s*\\[\\s*"
                + columns
                + "\\s*]"),
        Pattern.compile(
            type
                + "\\s+"
                + variablePattern
                + "\\s*\\[\\s*]\\s*\\[\\s*]\\s*=\\s*new\\s+"
                + type
                + "\\s*\\[\\s*"
                + rows
                + "\\s*]\\s*\\[\\s*"
                + columns
                + "\\s*]"));
  }

  private static CodeLine intArrayLiteral(String variable, String... values) {
    return arrayLiteral("int", variable, values);
  }

  private static CodeLine arrayLiteral(String type, String variable, String... values) {
    return new CodeLine(
        Pattern.compile(
            type
                + "\\s*\\[\\s*]\\s*"
                + variable
                + "\\s*=\\s*\\{\\s*"
                + String.join("\\s*,\\s*", values)
                + "\\s*}"),
        Pattern.compile(
            type
                + "\\s+"
                + variable
                + "\\s*\\[\\s*]\\s*=\\s*\\{\\s*"
                + String.join("\\s*,\\s*", values)
                + "\\s*}"));
  }

  /**
   * Creates an array-literal line whose values may appear in any order.
   *
   * @param type array element type
   * @param variable array variable name
   * @param values accepted literal values
   * @return a code line accepting all value permutations
   */
  private static CodeLine unorderedArrayLiteral(String type, String variable, String... values) {
    Set<String> permutations = new LinkedHashSet<>();
    addPermutations(values.clone(), 0, permutations);

    return new CodeLine(
        permutations.stream()
            .flatMap(
                permutation ->
                    java.util.stream.Stream.of(
                        Pattern.compile(
                            type
                                + "\\s*\\[\\s*]\\s*"
                                + variable
                                + "\\s*=\\s*\\{\\s*"
                                + permutation
                                + "\\s*}"),
                        Pattern.compile(
                            type
                                + "\\s+"
                                + variable
                                + "\\s*\\[\\s*]\\s*=\\s*\\{\\s*"
                                + permutation
                                + "\\s*}")))
            .toArray(Pattern[]::new));
  }

  private static void addPermutations(String[] values, int start, Set<String> permutations) {
    if (start == values.length) {
      permutations.add(String.join("\\s*,\\s*", values));
      return;
    }
    for (int index = start; index < values.length; index++) {
      String value = values[start];
      values[start] = values[index];
      values[index] = value;
      addPermutations(values, start + 1, permutations);
      values[index] = values[start];
      values[start] = value;
    }
  }

  private static CodeLine assignment(String variable, int index, String value) {
    return codeLine(variable + "\\s*\\[\\s*" + index + "\\s*]\\s*=\\s*" + value);
  }

  private static CodeLine capturedAssignment(String variableCapture, int index, String value) {
    return codeLine(
        capturedIdentifier(variableCapture) + "\\s*\\[\\s*" + index + "\\s*]\\s*=\\s*" + value);
  }

  private static CodeLine twoDimensionalAssignment(
      String variable, int firstIndex, int secondIndex, String value) {
    return codeLine(
        variable
            + "\\s*\\[\\s*"
            + firstIndex
            + "\\s*]\\s*\\[\\s*"
            + secondIndex
            + "\\s*]\\s*=\\s*"
            + value);
  }

  private static CodeLine capturedTwoDimensionalAssignment(
      String variableCapture, int firstIndex, int secondIndex, String value) {
    return codeLine(
        capturedIdentifier(variableCapture)
            + "\\s*\\[\\s*"
            + firstIndex
            + "\\s*]\\s*\\[\\s*"
            + secondIndex
            + "\\s*]\\s*=\\s*"
            + value);
  }

  private static CodeLine twoDimensionalAccess(String variable, int firstIndex, int secondIndex) {
    return codeLine(
        variable + "\\s*\\[\\s*" + firstIndex + "\\s*]\\s*\\[\\s*" + secondIndex + "\\s*]");
  }

  private static CodeLine capturedTwoDimensionalAccess(
      String variableCapture, int firstIndex, int secondIndex) {
    return codeLine(
        capturedIdentifier(variableCapture)
            + "\\s*\\[\\s*"
            + firstIndex
            + "\\s*]\\s*\\[\\s*"
            + secondIndex
            + "\\s*]");
  }

  private static CodeLine lengthAccess(String variable) {
    return codeLine(variable + "\\s*\\.\\s*length");
  }

  private static CodeLine capturedLengthAccess(String variableCapture) {
    return codeLine(capturedIdentifier(variableCapture) + "\\s*\\.\\s*length");
  }

  private static CodeLine declaration(String type, String variable, String value) {
    return codeLine(type + "\\s+" + variable + "\\s*=\\s*" + value);
  }

  private static CodeLine declarationWithCapturedVariable(
      String type, String variableCapture, String value) {
    return codeLine(type + "\\s+" + capture(variableCapture) + "\\s*=\\s*" + value);
  }

  private static CodeLine enhancedForLoop(String type, String sourceArray, String itemCapture) {
    return codeLine(
        "for\\s*\\(\\s*"
            + type
            + "\\s+"
            + capture(itemCapture)
            + "\\s*:\\s*"
            + sourceArray
            + "\\s*\\)\\s*\\{");
  }

  private static CodeLine notNullCondition(String itemCapture) {
    return codeLine(
        "if\\s*\\(\\s*" + capturedIdentifier(itemCapture) + "\\s*!=\\s*null\\s*\\)\\s*\\{");
  }

  private static CodeLine increment(String variable) {
    return new CodeLine(
        Pattern.compile(variable + "\\s*\\+\\+"),
        Pattern.compile("\\+\\+" + variable),
        Pattern.compile(variable + "\\s*\\+=\\s*1"),
        Pattern.compile(variable + "\\s*=\\s*" + variable + "\\s*\\+\\s*1"));
  }

  private static CodeLine indexedForLoop(String arrayVariable, String indexCapture) {
    return new CodeLine(
        Pattern.compile(
            indexedForLoopRegex(indexCapture, "<", arrayVariable + "\\s*\\.\\s*length")),
        Pattern.compile(
            indexedForLoopRegex(
                indexCapture, "<=", arrayVariable + "\\s*\\.\\s*length\\s*-\\s*1")));
  }

  private static CodeLine indexedForLoopWithUpperBound(String indexCapture, String upperBound) {
    return codeLine(
        "for\\s*\\(\\s*int\\s+"
            + capture(indexCapture)
            + "\\s*=\\s*0\\s*;\\s*"
            + backReference(indexCapture)
            + "\\s*<\\s*"
            + upperBound
            + "\\s*;\\s*"
            + incrementExpression(indexCapture)
            + "\\s*\\)\\s*\\{");
  }

  private static CodeLine bubbleSortOuterLoop(String arrayCapture, String indexCapture) {
    return new CodeLine(
        Pattern.compile(
            indexedForLoopRegex(
                indexCapture,
                "<",
                capturedIdentifier(arrayCapture) + "\\s*\\.\\s*length\\s*-\\s*1")),
        Pattern.compile(
            indexedForLoopRegex(
                indexCapture,
                "<=",
                capturedIdentifier(arrayCapture) + "\\s*\\.\\s*length\\s*-\\s*2")));
  }

  private static CodeLine bubbleSortInnerLoop(
      String arrayCapture, String outerIndexCapture, String innerIndexCapture) {
    return new CodeLine(
        Pattern.compile(
            indexedForLoopRegex(
                innerIndexCapture,
                "<",
                capturedIdentifier(arrayCapture)
                    + "\\s*\\.\\s*length\\s*-\\s*1\\s*-\\s*"
                    + capturedIdentifier(outerIndexCapture))),
        Pattern.compile(
            indexedForLoopRegex(
                innerIndexCapture,
                "<=",
                capturedIdentifier(arrayCapture)
                    + "\\s*\\.\\s*length\\s*-\\s*2\\s*-\\s*"
                    + capturedIdentifier(outerIndexCapture))));
  }

  private static CodeLine methodCallWithArrayAccess(
      String receiver, String method, String arrayVariable, String indexCapture) {
    return codeLine(
        receiver
            + "\\s*\\.\\s*"
            + method
            + "\\s*\\(\\s*"
            + arrayVariable
            + "\\s*\\[\\s*"
            + capturedIdentifier(indexCapture)
            + "\\s*]\\s*\\)");
  }

  private static CodeLine methodCall(String receiver, String method) {
    return codeLine(receiver + "\\s*\\.\\s*" + method + "\\s*\\(\\s*\\)");
  }

  private static CodeLine outerTwoDimensionalLoop(String arrayVariable, String rowCapture) {
    return new CodeLine(
        Pattern.compile(indexedForLoopRegex(rowCapture, "<", arrayVariable + "\\s*\\.\\s*length")),
        Pattern.compile(
            indexedForLoopRegex(rowCapture, "<=", arrayVariable + "\\s*\\.\\s*length\\s*-\\s*1")));
  }

  private static CodeLine innerTwoDimensionalLoop(
      String arrayVariable, String rowCapture, String columnCapture) {
    String upperBound =
        arrayVariable + "\\s*\\[\\s*" + capturedIdentifier(rowCapture) + "\\s*]\\s*\\.\\s*length";
    return new CodeLine(
        Pattern.compile(indexedForLoopRegex(columnCapture, "<", upperBound)),
        Pattern.compile(indexedForLoopRegex(columnCapture, "<=", upperBound + "\\s*-\\s*1")));
  }

  private static CodeLine twoDimensionalEqualsCondition(
      String arrayVariable, String rowCapture, String columnCapture, String value) {
    return codeLine(
        "if\\s*\\(\\s*"
            + arrayVariable
            + "\\s*\\[\\s*"
            + capturedIdentifier(rowCapture)
            + "\\s*]\\s*\\[\\s*"
            + capturedIdentifier(columnCapture)
            + "\\s*]\\s*==\\s*"
            + value
            + "\\s*\\)\\s*\\{");
  }

  private static CodeLine arrayGreaterThanNextCondition(String arrayCapture, String indexCapture) {
    return codeLine(
        "if\\s*\\(\\s*"
            + arrayAccess(arrayCapture, indexCapture)
            + "\\s*>\\s*"
            + nextArrayAccessReference(arrayCapture, indexCapture)
            + "\\s*\\)\\s*\\{");
  }

  private static String arrayAccess(String arrayCapture, String indexCapture) {
    return capturedIdentifier(arrayCapture)
        + "\\s*\\[\\s*"
        + capturedIdentifier(indexCapture)
        + "\\s*]";
  }

  private static String nextArrayAccess(String arrayCapture, String indexCapture) {
    return capturedIdentifier(arrayCapture)
        + "\\s*\\[\\s*"
        + capturedIdentifier(indexCapture)
        + "\\s*\\+\\s*1\\s*]";
  }

  private static String nextArrayAccessReference(String arrayCapture, String indexCapture) {
    return backReference(arrayCapture)
        + "\\s*\\[\\s*"
        + backReference(indexCapture)
        + "\\s*\\+\\s*1\\s*]";
  }

  private static CodeLine assignment(String leftSide, String rightSide) {
    return codeLine(leftSide + "\\s*=\\s*" + rightSide);
  }

  private static String capture(String name) {
    return "(?<" + name + ">" + IDENTIFIER + ")";
  }

  private static String capturedIdentifier(String name) {
    return "(?<" + name + ">" + IDENTIFIER + ")";
  }

  private static String backReference(String name) {
    return "\\k<" + name + ">";
  }

  private static CodeLine codeLine(String regex) {
    return new CodeLine(Pattern.compile(regex));
  }

  private static String indexedForLoopRegex(
      String indexCapture, String comparisonOperator, String upperBound) {
    return "for\\s*\\(\\s*int\\s+"
        + capture(indexCapture)
        + "\\s*=\\s*0\\s*;\\s*"
        + backReference(indexCapture)
        + "\\s*"
        + comparisonOperator
        + "\\s*"
        + upperBound
        + "\\s*;\\s*"
        + incrementExpression(indexCapture)
        + "\\s*\\)\\s*\\{";
  }

  private static String incrementExpression(String variableCapture) {
    return "(?:"
        + backReference(variableCapture)
        + "\\+\\+|\\+\\+"
        + backReference(variableCapture)
        + "|"
        + backReference(variableCapture)
        + "\\s*\\+=\\s*1|"
        + backReference(variableCapture)
        + "\\s*=\\s*"
        + backReference(variableCapture)
        + "\\s*\\+\\s*1"
        + ")";
  }
}
