package rooms.systemRecovery.util.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Specifies the accepted ways to build the module array in riddle 2. */
public class ModuleArrayInputModesTest {

  private final TerminalInterpreter interpreter = TerminalInterpreter.instance();

  /** Resets the real terminal sequence and advances it to riddle 2. */
  @BeforeEach
  public void setup() {
    interpreter.reset();
    TerminalInterpreterSetup.setupPreviewStates();
    assertTrue(interpreter.interpret("int[] energie = new int[5];"));
    assertTrue(
        interpreter.interpret(
            "energie[0] = 40; energie[1] = 10; energie[2] = 80; "
                + "energie[3] = 30; energie[4] = 60;"));
    assertEquals(2, interpreter.currentState());
  }

  /** Keeps the original two-send route and verifies assignment order is unrestricted. */
  @Test
  public void acceptsArrayCreationThenAllValuesInAnyAssignmentOrder() {
    assertTrue(interpreter.interpret("String[] module = new String[5];"));
    assertEquals(3, interpreter.currentState());

    assertTrue(
        interpreter.interpret(
            "module[3] = \"SSD\"; module[0] = \"CPU\"; module[4] = \"NETWORK\"; "
                + "module[2] = \"GPU\"; module[1] = \"RAM\";"));
    assertEquals(4, interpreter.currentState());
  }

  /** Allows one correct array-slot assignment per submission until all five are present. */
  @Test
  public void acceptsEachCorrectValueAsAnIndividualSubmission() {
    assertTrue(interpreter.interpret("String[] module = new String[5];"));

    List<String> assignments =
        List.of(
            "module[3] = \"SSD\";",
            "module[0] = \"CPU\";",
            "module[4] = \"NETWORK\";",
            "module[1] = \"RAM\";",
            "module[2] = \"GPU\";");

    for (int index = 0; index < assignments.size(); index++) {
      assertTrue(interpreter.interpret(assignments.get(index)), assignments.get(index));
      assertEquals(index == assignments.size() - 1 ? 4 : 3, interpreter.currentState());
    }
  }

  /** Accepts the editor's full growing source when it retains earlier submissions. */
  @Test
  public void acceptsCumulativeEditorSourceAcrossIndividualSends() {
    String source = "String[] module = new String[5];";
    assertTrue(interpreter.interpret(source));

    List<String> assignments =
        List.of(
            "module[3] = \"SSD\";",
            "module[0] = \"CPU\";",
            "module[4] = \"NETWORK\";",
            "module[1] = \"RAM\";",
            "module[2] = \"GPU\";");
    for (int index = 0; index < assignments.size(); index++) {
      source += "\n" + assignments.get(index);
      assertTrue(interpreter.interpret(source), assignments.get(index));
      assertEquals(index == assignments.size() - 1 ? 4 : 3, interpreter.currentState());
    }
  }

  /** Accepts the compact Java array literal and completes both logical terminal steps. */
  @Test
  public void acceptsCompactArrayLiteralAndCompletesBothModuleSteps() {
    assertTrue(
        interpreter.interpret(
            "String[] module = {\"CPU\", \"RAM\", \"GPU\", \"SSD\", \"NETWORK\"};"));
    assertEquals(4, interpreter.currentState());
  }

  /** Accepts Java's explicit {@code new String[]{...}} array initializer form. */
  @Test
  public void acceptsExplicitArrayLiteralForm() {
    assertTrue(
        interpreter.interpret(
            "String[] module = new String[]{\"CPU\", \"RAM\", \"GPU\", \"SSD\", "
                + "\"NETWORK\"};"));
    assertEquals(4, interpreter.currentState());
  }

  /** Accepts a declaration and all indexed assignments in one source submission. */
  @Test
  public void acceptsDeclarationAndAssignmentsTogetherAndCompletesBothSteps() {
    assertTrue(
        interpreter.interpret(
            "String[] module = new String[5];\n"
                + "module[3] = \"SSD\";\n"
                + "module[0] = \"CPU\";\n"
                + "module[4] = \"NETWORK\";\n"
                + "module[2] = \"GPU\";\n"
                + "module[1] = \"RAM\";"));
    assertEquals(4, interpreter.currentState());
  }

  /** Does not accept an array whose values are assigned to incorrect indices. */
  @Test
  public void rejectsValuesAssignedToTheWrongIndices() {
    assertFalse(
        interpreter.interpret(
            "String[] module = {\"CPU\", \"GPU\", \"RAM\", \"SSD\", \"NETWORK\"};"));
    assertEquals(2, interpreter.currentState());
  }

  /** Preserves previously accepted slot values after a later invalid assignment. */
  @Test
  public void wrongAssignmentDoesNotDiscardEarlierPartialValues() {
    assertTrue(interpreter.interpret("String[] module = new String[5];"));
    assertTrue(interpreter.interpret("module[3] = \"SSD\";"));

    assertFalse(interpreter.interpret("module[0] = \"RAM\";"));
    assertEquals(3, interpreter.currentState());

    assertTrue(interpreter.interpret("module[0] = \"CPU\";"));
    assertTrue(interpreter.interpret("module[4] = \"NETWORK\";"));
    assertTrue(interpreter.interpret("module[1] = \"RAM\";"));
    assertTrue(interpreter.interpret("module[2] = \"GPU\";"));
    assertEquals(4, interpreter.currentState());
  }

  /** Rebuilds the completed array from its accepted individual submissions after loading. */
  @Test
  public void individualSubmissionsAreReconstructableFromAcceptedInputHistory() {
    assertTrue(interpreter.interpret("String[] module = new String[5];"));
    for (String assignment :
        List.of(
            "module[3] = \"SSD\";",
            "module[0] = \"CPU\";",
            "module[4] = \"NETWORK\";",
            "module[1] = \"RAM\";",
            "module[2] = \"GPU\";")) {
      assertTrue(interpreter.interpret(assignment));
    }

    List<TerminalInterpreter.AcceptedInput> acceptedInputs = interpreter.acceptedInputs();
    assertEquals(4, acceptedInputs.size());
    assertTrue(acceptedInputs.get(3).source().contains("module[3] = \"SSD\";"));
    assertTrue(acceptedInputs.get(3).source().contains("module[2] = \"GPU\";"));

    interpreter.reset();
    TerminalInterpreterSetup.setupPreviewStates();
    interpreter.restoreAcceptedInputs(acceptedInputs);

    assertEquals(4, interpreter.currentState());
  }
}
