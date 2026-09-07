package rooms.systemRecovery.util.interpreter;

import java.util.regex.Pattern;
import rooms.systemRecovery.modules.interpreter.CodeLine;
import rooms.systemRecovery.modules.interpreter.TerminalCodeRequirement;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Sets up the interpreter states used by the System Recovery terminal puzzle. */
public final class TerminalInterpreterSetup {

  private static final String IDENTIFIER = "[a-zA-Z][a-zA-Z0-9]*";
  private static final Runnable SUCCESS = InterpretationCallbacks::showCorrectTerminalInputDialog;
  private static final Runnable FAILURE = InterpretationCallbacks::showIncorrectTerminalInputDialog;
  private static final String MODULE_ARRAY = "moduleArray";
  private static final String STORAGE_ARRAY = "storageArray";

  private static final int RIDDLE_ONE_STEP_ONE = 0;
  private static final int RIDDLE_ONE_STEP_TWO = 1;
  private static final int RIDDLE_TWO_STEP_ONE = 2;
  private static final int RIDDLE_TWO_STEP_TWO = 3;
  private static final int RIDDLE_TWO_STEP_THREE = 4;
  private static final int RIDDLE_TWO_STEP_FOUR = 5;
  private static final int RIDDLE_THREE_STEP_ONE = 6;
  private static final int RIDDLE_FOUR_STEP_ONE = 7;
  private static final int RIDDLE_FOUR_STEP_TWO = 8;
  private static final int RIDDLE_SEVEN_STEP_ONE = 9;
  private static final int RIDDLE_EIGHT_STEP_ONE = 10;
  private static final int RIDDLE_EIGHT_STEP_TWO = 11;
  private static final int RIDDLE_EIGHT_STEP_THREE = 12;
  private static final int RIDDLE_NINE_STEP_ONE = 13;
  private static final int RIDDLE_TEN_STEP_ONE = 14;
  private static final int RIDDLE_TEN_STEP_TWO = 15;
  private static final int RIDDLE_TEN_STEP_THREE = 16;

  private TerminalInterpreterSetup() {}

  /** Sets up no-op callbacks so the client can preview interpretation feedback. */
  public static void setupPreviewStates() {
    setupAllTerminalRiddles(null, null);
  }

  /** Registers all terminal riddles in their gameplay order. */
  public static void setupRoomStates() {
    setupAllTerminalRiddles(SUCCESS, FAILURE);
  }

  private static void setupAllTerminalRiddles(Runnable onSuccess, Runnable onFailure) {
    setupRiddleOneMaterializationChamber(onSuccess, onFailure);
    setupRiddleTwoDefectiveModuleStorage(onSuccess, onFailure);
    setupRiddleThreeInventoryScanner(onSuccess, onFailure);
    setupRiddleFourTransportStorage(onSuccess, onFailure);
    setupRiddleFiveChaoticDataStorage();
    setupRiddleSixBubbleSortMachine();
    setupRiddleSevenDataArchive(onSuccess, onFailure);
    setupRiddleEightTwoDimensionalStorage(onSuccess, onFailure);
    setupRiddleNineSearchRobot(onSuccess, onFailure);
    setupRiddleTenCentralDataCenter(onSuccess, onFailure);
  }

  private static void setupRiddleOneMaterializationChamber(Runnable onSuccess, Runnable onFailure) {
    setupRiddleOneStepOneInitializeEnergyArray(
        () -> InterpretationCallbacks.spawnEnergieCrates(), onFailure);
    setupRiddleOneStepTwoSetEnergyValues(onSuccess, onFailure);
  }

  private static void setupRiddleOneStepOneInitializeEnergyArray(
      Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_ONE_STEP_ONE, unordered(onSuccess, onFailure, arrayCreation("int", "energie", 5)));
  }

  private static void setupRiddleOneStepTwoSetEnergyValues(Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_ONE_STEP_TWO,
        unordered(
            () -> InterpretationCallbacks.markEnergyCratesCorrect(),
            onFailure,
            assignment("energie", 0, "40"),
            assignment("energie", 1, "10"),
            assignment("energie", 2, "80"),
            assignment("energie", 3, "30"),
            assignment("energie", 4, "60")));
  }

  private static void setupRiddleTwoDefectiveModuleStorage(Runnable onSuccess, Runnable onFailure) {
    setupRiddleTwoStepOneInitializeModuleArray(onSuccess, onFailure);
    setupRiddleTwoStepTwoSetModules(onSuccess, onFailure);
    setupRiddleTwoStepThreeRemoveGpuModule(onSuccess, onFailure);
    setupRiddleTwoStepFourReadModuleLength(onSuccess, onFailure);
  }

  private static void setupRiddleTwoStepOneInitializeModuleArray(
      Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_TWO_STEP_ONE,
        unordered(onSuccess, onFailure, arrayCreation("String", MODULE_ARRAY, 5, true)));
  }

  private static void setupRiddleTwoStepTwoSetModules(Runnable onSuccess, Runnable onFailure) {
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
      Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_TWO_STEP_THREE,
        unordered(onSuccess, onFailure, capturedAssignment(MODULE_ARRAY, 2, "null")));
  }

  private static void setupRiddleTwoStepFourReadModuleLength(
      Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_TWO_STEP_FOUR, unordered(onSuccess, onFailure, capturedLengthAccess(MODULE_ARRAY)));
  }

  private static void setupRiddleThreeInventoryScanner(Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_THREE_STEP_ONE,
        ordered(
            onSuccess,
            onFailure,
            declaration("int", "count", "0"),
            enhancedForLoop("String", capturedIdentifier(MODULE_ARRAY), "moduleItem"),
            notNullCondition("moduleItem"),
            increment("count")));
  }

  private static void setupRiddleFourTransportStorage(Runnable onSuccess, Runnable onFailure) {
    setupRiddleFourStepOneCreatePackages(onSuccess, onFailure);
    setupRiddleFourStepTwoTransportPackages(onSuccess, onFailure);
  }

  private static void setupRiddleFourStepOneCreatePackages(Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_FOUR_STEP_ONE,
        unordered(onSuccess, onFailure, intArrayLiteral("pakete", "15", "40", "20", "60", "30")));
  }

  private static void setupRiddleFourStepTwoTransportPackages(
      Runnable onSuccess, Runnable onFailure) {
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

  private static void setupRiddleSevenDataArchive(Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_SEVEN_STEP_ONE,
        unordered(
            onSuccess,
            onFailure,
            intArrayLiteral("energie", "20", "50", "80"),
            stringArrayLiteral("module", "CPU", "GPU", "RAM"),
            booleanArrayLiteral("aktiv", "true", "false", "true")));
  }

  private static void setupRiddleEightTwoDimensionalStorage(
      Runnable onSuccess, Runnable onFailure) {
    setupRiddleEightStepOneCreateStorage(onSuccess, onFailure);
    setupRiddleEightStepTwoFillStorage(onSuccess, onFailure);
    setupRiddleEightStepThreeReadStorage(onSuccess, onFailure);
  }

  private static void setupRiddleEightStepOneCreateStorage(Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_EIGHT_STEP_ONE,
        unordered(
            onSuccess, onFailure, twoDimensionalArrayCreation("int", STORAGE_ARRAY, 3, 4, true)));
  }

  private static void setupRiddleEightStepTwoFillStorage(Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_EIGHT_STEP_TWO,
        unordered(
            onSuccess,
            onFailure,
            capturedTwoDimensionalAssignment(STORAGE_ARRAY, 0, 2, "1"),
            capturedTwoDimensionalAssignment(STORAGE_ARRAY, 1, 3, "2"),
            capturedTwoDimensionalAssignment(STORAGE_ARRAY, 2, 1, "3")));
  }

  private static void setupRiddleEightStepThreeReadStorage(Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_EIGHT_STEP_THREE,
        unordered(onSuccess, onFailure, capturedTwoDimensionalAccess(STORAGE_ARRAY, 1, 3)));
  }

  private static void setupRiddleNineSearchRobot(Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_NINE_STEP_ONE,
        ordered(
            onSuccess,
            onFailure,
            outerTwoDimensionalLoop("map", "mapRow"),
            innerTwoDimensionalLoop("map", "mapRow", "mapColumn"),
            twoDimensionalEqualsCondition("map", "mapRow", "mapColumn", "1"),
            methodCall("roboter", "collect")));
  }

  private static void setupRiddleTenCentralDataCenter(Runnable onSuccess, Runnable onFailure) {
    setupRiddleTenStepOneBubbleSort(onSuccess, onFailure);
    setupRiddleTenStepTwoCountModules(onSuccess, onFailure);
    setupRiddleTenStepThreeFindBatteries(onSuccess, onFailure);
  }

  private static void setupRiddleTenStepOneBubbleSort(Runnable onSuccess, Runnable onFailure) {
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

  private static void setupRiddleTenStepTwoCountModules(Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_TEN_STEP_TWO,
        ordered(
            onSuccess,
            onFailure,
            declaration("int", "count", "0"),
            enhancedForLoop("String", "modules", "moduleItem"),
            notNullCondition("moduleItem"),
            increment("count")));
  }

  private static void setupRiddleTenStepThreeFindBatteries(Runnable onSuccess, Runnable onFailure) {
    register(
        RIDDLE_TEN_STEP_THREE,
        ordered(
            onSuccess,
            onFailure,
            outerTwoDimensionalLoop("map", "mapRow"),
            innerTwoDimensionalLoop("map", "mapRow", "mapColumn"),
            twoDimensionalEqualsCondition("map", "mapRow", "mapColumn", "1"),
            methodCall("roboter", "collect")));
  }

  private static TerminalCodeRequirement unordered(
      Runnable onSuccess, Runnable onFailure, CodeLine... codeLines) {
    return new TerminalCodeRequirement(codeLines, false, onSuccess, onFailure);
  }

  private static TerminalCodeRequirement ordered(
      Runnable onSuccess, Runnable onFailure, CodeLine... codeLines) {
    return new TerminalCodeRequirement(codeLines, true, onSuccess, onFailure);
  }

  private static void register(int state, TerminalCodeRequirement requirement) {
    TerminalInterpreter.instance().register(state, requirement);
  }

  private static CodeLine arrayCreation(String type, String variable, int size) {
    return arrayCreation(type, variable, size, false);
  }

  private static CodeLine arrayCreation(String type, String variable, int size, boolean captured) {
    String variablePattern = captured ? capture(variable) : variable;
    return codeLine(
        type
            + "\\s*\\[\\s*]\\s*"
            + variablePattern
            + "\\s*=\\s*new\\s+"
            + type
            + "\\s*\\[\\s*"
            + size
            + "\\s*]");
  }

  private static CodeLine twoDimensionalArrayCreation(
      String type, String variable, int rows, int columns) {
    return twoDimensionalArrayCreation(type, variable, rows, columns, false);
  }

  private static CodeLine twoDimensionalArrayCreation(
      String type, String variable, int rows, int columns, boolean captured) {
    String variablePattern = captured ? capture(variable) : variable;
    return codeLine(
        type
            + "\\s*\\[\\s*]\\s*\\[\\s*]\\s*"
            + variablePattern
            + "\\s*=\\s*new\\s+"
            + type
            + "\\s*\\[\\s*"
            + rows
            + "\\s*]\\s*\\[\\s*"
            + columns
            + "\\s*]");
  }

  private static CodeLine intArrayLiteral(String variable, String... values) {
    return arrayLiteral("int", variable, values);
  }

  private static CodeLine stringArrayLiteral(String variable, String... values) {
    String[] quotedValues = new String[values.length];
    for (int index = 0; index < values.length; index++) {
      quotedValues[index] = "\"" + values[index] + "\"";
    }
    return arrayLiteral("String", variable, quotedValues);
  }

  private static CodeLine booleanArrayLiteral(String variable, String... values) {
    return arrayLiteral("boolean", variable, values);
  }

  private static CodeLine arrayLiteral(String type, String variable, String... values) {
    return codeLine(
        type
            + "\\s*\\[\\s*]\\s*"
            + variable
            + "\\s*=\\s*\\{\\s*"
            + String.join("\\s*,\\s*", values)
            + "\\s*}");
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
    return indexedForLoopWithUpperBound(indexCapture, arrayVariable + "\\s*\\.\\s*length");
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
    return codeLine(
        "for\\s*\\(\\s*int\\s+"
            + capture(rowCapture)
            + "\\s*=\\s*0\\s*;\\s*"
            + backReference(rowCapture)
            + "\\s*<\\s*"
            + arrayVariable
            + "\\s*\\.\\s*length\\s*;\\s*"
            + incrementExpression(rowCapture)
            + "\\s*\\)\\s*\\{");
  }

  private static CodeLine innerTwoDimensionalLoop(
      String arrayVariable, String rowCapture, String columnCapture) {
    return codeLine(
        "for\\s*\\(\\s*int\\s+"
            + capture(columnCapture)
            + "\\s*=\\s*0\\s*;\\s*"
            + backReference(columnCapture)
            + "\\s*<\\s*"
            + arrayVariable
            + "\\s*\\[\\s*"
            + capturedIdentifier(rowCapture)
            + "\\s*]\\s*\\.\\s*length\\s*;\\s*"
            + incrementExpression(columnCapture)
            + "\\s*\\)\\s*\\{");
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
        + ")";
  }
}
