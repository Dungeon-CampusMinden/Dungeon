package rooms.systemRecovery.util.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Covers direct and cached array-length syntax across System Recovery array-loop puzzles. */
class TerminalArrayLengthSyntaxTest {

  private final TerminalInterpreter interpreter = TerminalInterpreter.instance();

  @BeforeEach
  void setUp() {
    interpreter.reset();
    TerminalInterpreterSetup.setupPreviewStates();
  }

  @Test
  void moduleLengthMustBeShownAndAllowsEitherTemporaryVariableForm() {
    int state = TerminalStep.MODULE_LENGTH.stateId();

    assertTrue(interpreter.analyzeState(state, "display.show(module.length);"));
    assertTrue(
        interpreter.analyzeState(state, "int capacity = module.length; display.show(capacity);"));
    assertTrue(
        interpreter.analyzeState(
            state, "int capacity = 0; capacity = module.length; display.show(capacity);"));
    assertFalse(interpreter.analyzeState(state, "module.length;"));
  }

  @Test
  void moduleLengthRejectsDifferentArrayAfterItsNameHasBeenEstablished() {
    assertTrue(interpreter.interpret("int[] energie = new int[5];"));
    assertTrue(
        interpreter.interpret(
            "energie[0]=40; energie[1]=10; energie[2]=80; energie[3]=30; energie[4]=60;"));
    assertTrue(interpreter.interpret("String[] module = new String[5];"));
    assertTrue(
        interpreter.interpret(
            "module[0]=\"CPU\"; module[1]=\"RAM\"; module[2]=\"GPU\"; "
                + "module[3]=\"SSD\"; module[4]=\"NETWORK\";"));
    assertTrue(interpreter.interpret("module[2] = null;"));

    assertFalse(interpreter.interpret("int capacity = other.length; display.show(capacity);"));
    assertFalse(
        interpreter.interpret(
            "int CPU = other.length; module[0] = \"CPU\"; display.show(module.length);"));
  }

  @Test
  void acceptedLengthAliasKeepsThePlayersExactSource() {
    int state = TerminalStep.MODULE_LENGTH.stateId();
    String source = "int amount = 0; amount = module.length; display.show(amount);";

    interpreter.synchronizeState(state);
    assertTrue(interpreter.interpret(source));
    assertEquals(source, interpreter.acceptedInputs().getFirst().source());
  }

  @Test
  void inventoryCounterMustBeDisplayedAfterTheLoop() {
    int state = TerminalStep.INVENTORY_COUNT.stateId();
    String loop =
        "int occupied = 0; for (String item : module) { if (item != null) { occupied++; } } ";

    assertTrue(interpreter.analyzeState(state, loop + "display.show(occupied);"));
    assertFalse(interpreter.analyzeState(state, loop));
    assertFalse(
        interpreter.analyzeState(
            state,
            "int occupied = 0; for (String item : module) { "
                + "if (item != null) { occupied++; display.show(occupied); } }"));
  }

  @Test
  void packageLoopAcceptsDirectAndCachedArrayLength() {
    int state = TerminalStep.TRANSPORT_COLLECT.stateId();

    assertTrue(
        interpreter.analyzeState(
            state, "for (int i = 0; i < pakete.length; i++) { roboter.collect(); }"));
    assertTrue(
        interpreter.analyzeState(
            state,
            "int total = pakete.length; "
                + "for (int i = 0; i < total; i++) { roboter.collect(); }"));
    assertTrue(
        interpreter.analyzeState(
            state,
            "int total = 0; total = pakete.length; "
                + "for (int i = 0; i < total; i++) { roboter.collect(); }"));
  }

  @Test
  void bubbleSortLoopsAcceptCachedArrayLength() {
    int state = TerminalInterpreterSetup.CENTRAL_SORT_STATE;
    String body =
        "if (array[j] > array[j + 1]) { int temp = array[j]; "
            + "array[j] = array[j + 1]; array[j + 1] = temp; }";
    String inner = "for (int j = 0; j < size - 1 - i; j++) { " + body + " }";
    String sort = "for (int i = 0; i < size - 1; i++) { " + inner + " }";

    assertTrue(interpreter.analyzeState(state, "int size = array.length; " + sort));
    assertTrue(interpreter.analyzeState(state, "int size = 0; size = array.length; " + sort));
  }

  @Test
  void nestedSearchLoopsAcceptPerRowCachedLengths() {
    String source =
        "int rows = map.length; for (int i = 0; i < rows; i++) { "
            + "int columns = map[i].length; for (int j = 0; j < columns; j++) { "
            + "if (map[i][j] == 1) { roboter.collect(); } } }";
    String zeroInitializedSource =
        "int rows = 0; rows = map.length; for (int i = 0; i < rows; i++) { "
            + "int columns = 0; columns = map[i].length; "
            + "for (int j = 0; j < columns; j++) { "
            + "if (map[i][j] == 1) { roboter.collect(); } } }";

    assertTrue(TerminalInterpreterSetup.matchesSearchRobotProgram(source));
    assertTrue(TerminalInterpreterSetup.matchesSearchRobotProgram(zeroInitializedSource));
  }

  @Test
  void centralGridLoopAcceptsCachedRowAndColumnLengths() {
    int state = TerminalStep.CENTRAL_SEARCH.stateId();
    String source =
        "int rowCount = map.length; for (int row = 0; row < rowCount; row++) { "
            + "int columnCount = map[row].length; "
            + "for (int column = 0; column < columnCount; column++) { roboter.collect(); } }";

    assertTrue(interpreter.analyzeState(state, source));
  }
}
