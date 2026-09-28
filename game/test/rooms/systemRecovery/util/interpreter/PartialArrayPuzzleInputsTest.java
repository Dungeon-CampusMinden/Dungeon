package rooms.systemRecovery.util.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.interpreter.CodeLine;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.modules.interpreter.TerminalCodeRequirement;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Covers incremental array entry in the energy, archive and two-dimensional-storage riddles. */
class PartialArrayPuzzleInputsTest {

  private final TerminalInterpreter interpreter = TerminalInterpreter.instance();

  @BeforeEach
  void setUp() {
    interpreter.reset();
    TerminalInterpreterSetup.setupPreviewStates();
  }

  @Test
  void energyValuesCanBeSubmittedOneAtATimeInAnyOrder() {
    assertTrue(interpreter.interpret("int[] energie = new int[5];"));

    List<String> assignments =
        List.of(
            "energie[3] = 30;",
            "energie[0] = 40;",
            "energie[4] = 60;",
            "energie[2] = 80;",
            "energie[1] = 10;");
    for (int index = 0; index < assignments.size(); index++) {
      assertTrue(interpreter.interpret(assignments.get(index)), assignments.get(index));
      assertEquals(index == assignments.size() - 1 ? 2 : 1, interpreter.currentState());
    }

    assertTrue(interpreter.acceptedInputs().get(1).source().contains(assignments.getFirst()));
    assertTrue(interpreter.acceptedInputs().get(1).source().contains(assignments.getLast()));
  }

  @Test
  void energyValuesAlsoWorkWhenTheEditorRetainsEarlierLines() {
    assertTrue(interpreter.interpret("int[] energie = new int[5];"));
    String source = "energie[2] = 80;";
    assertTrue(interpreter.interpret(source));
    source += "\nenergie[0] = 40;";
    assertTrue(interpreter.interpret(source));
    source += "\nenergie[4] = 60;";
    assertTrue(interpreter.interpret(source));
    source += "\nenergie[1] = 10;";
    assertTrue(interpreter.interpret(source));
    source += "\nenergie[3] = 30;";
    assertTrue(interpreter.interpret(source));

    assertEquals(source, interpreter.acceptedInputs().get(1).source());
    assertEquals(2, interpreter.currentState());
  }

  @Test
  void completedAttemptKeepsTheLastSubmissionAndTheFullAcceptedSource() {
    List<TerminalAttempt> attempts = new ArrayList<>();
    interpreter.reset();
    interpreter.register(
        0,
        new TerminalCodeRequirement(
                new CodeLine[] {
                  new CodeLine(Pattern.compile("first;")),
                  new CodeLine(Pattern.compile("second;"))
                },
                false,
                attempts::add,
                null)
            .withPartialSubmissions(ignored -> {}));

    assertTrue(interpreter.interpret("first;"));
    assertTrue(interpreter.interpret("second;"));

    TerminalAttempt completed = attempts.getFirst();
    assertEquals("second;", completed.source());
    assertEquals("first;\nsecond;", completed.acceptedSource());
  }

  @Test
  void archiveArraysCanBeDeclaredAndFilledInInterleavedOrderAtArbitraryIndexes() {
    advanceToArchive();
    assertEquals(9, interpreter.currentState());

    List<String> sourceParts =
        List.of(
            "String[] module = new String[3];",
            "module[2] = \"RAM\";",
            "boolean[] aktiv = new boolean[3];",
            "aktiv[0] = true;",
            "int[] energie = new int[3];",
            "energie[1] = 80;",
            "module[0] = \"CPU\";",
            "aktiv[2] = false;",
            "energie[2] = 20;",
            "module[1] = \"GPU\";",
            "aktiv[1] = true;",
            "energie[0] = 50;");

    for (int index = 0; index < sourceParts.size(); index++) {
      assertTrue(interpreter.interpret(sourceParts.get(index)), sourceParts.get(index));
      assertEquals(index == sourceParts.size() - 1 ? 10 : 9, interpreter.currentState());
    }

    String acceptedArchiveSource = interpreter.acceptedInputs().getLast().source();
    for (String sourcePart : sourceParts) {
      assertTrue(acceptedArchiveSource.contains(sourcePart), sourcePart);
    }

    List<TerminalInterpreter.AcceptedInput> acceptedInputs = interpreter.acceptedInputs();
    interpreter.reset();
    TerminalInterpreterSetup.setupPreviewStates();
    interpreter.restoreAcceptedInputs(acceptedInputs);
    assertEquals(10, interpreter.currentState());
  }

  @Test
  void completeArchiveSolutionIsCheckedIndependentlyOfEarlierPartialIndexes() {
    advanceToArchive();
    assertTrue(interpreter.interpret("int[] energie = new int[3];"));
    assertTrue(interpreter.interpret("energie[0] = 20;"));

    String fullSolution =
        "int[] energie = new int[3];\n"
            + "energie[2] = 20;\n"
            + "energie[0] = 50;\n"
            + "energie[1] = 80;\n"
            + "String[] module = {\"CPU\", \"GPU\", \"RAM\"};\n"
            + "boolean[] aktiv = {true, false, true};";
    assertTrue(interpreter.interpret(fullSolution));
    assertEquals(10, interpreter.currentState());
    assertEquals(fullSolution, interpreter.acceptedInputs().getLast().source());
  }

  @Test
  void archiveRejectsAssignmentsBeforeDeclarationAndDuplicateIndexes() {
    advanceToArchive();
    assertFalse(interpreter.interpret("energie[0] = 20;"));
    assertEquals(9, interpreter.currentState());

    assertTrue(interpreter.interpret("int[] energie = new int[3];"));
    assertTrue(interpreter.interpret("energie[0] = 20;"));
    assertFalse(interpreter.interpret("energie[0] = 50;"));
    assertEquals(9, interpreter.currentState());
  }

  @Test
  void archiveStillAcceptsCompleteArrayLiteralsInAnyDeclarationOrder() {
    advanceToArchive();

    assertTrue(
        interpreter.interpret(
            "boolean[] aktiv = {false, true, true};\n"
                + "String[] module = {\"RAM\", \"CPU\", \"GPU\"};\n"
                + "int[] energie = {80, 20, 50};"));
    assertEquals(10, interpreter.currentState());
  }

  @Test
  void archiveAcceptsDeclarationsAndTheirAssignmentsInOneSubmission() {
    advanceToArchive();

    assertTrue(
        interpreter.interpret(
            "boolean[] aktiv = new boolean[3];\n"
                + "aktiv[1] = false;\n"
                + "int[] energie = new int[3];\n"
                + "energie[0] = 20;\n"
                + "String[] module = new String[3];\n"
                + "module[2] = \"GPU\";\n"
                + "aktiv[2] = true;\n"
                + "energie[2] = 80;\n"
                + "module[0] = \"RAM\";\n"
                + "aktiv[0] = true;\n"
                + "energie[1] = 50;\n"
                + "module[1] = \"CPU\";"));
    assertEquals(10, interpreter.currentState());
  }

  @Test
  void storageCellsCanBeSubmittedSeparatelyUntilAllThreeAreCorrect() {
    advanceToArchive();
    assertTrue(
        interpreter.interpret(
            "int[] energie = {20, 50, 80};\n"
                + "String[] module = {\"CPU\", \"GPU\", \"RAM\"};\n"
                + "boolean[] aktiv = {true, false, true};"));
    assertTrue(interpreter.interpret("int[][] lager = new int[3][4];"));

    List<String> assignments =
        List.of("lager[2][1] = 3;", "lager[0][2] = 1;", "lager[1][3] = 2;");
    for (int index = 0; index < assignments.size(); index++) {
      assertTrue(interpreter.interpret(assignments.get(index)), assignments.get(index));
      assertEquals(index == assignments.size() - 1 ? 12 : 11, interpreter.currentState());
    }
  }

  private void advanceToArchive() {
    assertTrue(interpreter.interpret("int[] energie = new int[5];"));
    assertTrue(
        interpreter.interpret(
            "energie[0] = 40; energie[1] = 10; energie[2] = 80; "
                + "energie[3] = 30; energie[4] = 60;"));
    assertTrue(interpreter.interpret("String[] module = new String[5];"));
    assertTrue(
        interpreter.interpret(
            "module[0] = \"CPU\"; module[1] = \"RAM\"; module[2] = \"GPU\"; "
                + "module[3] = \"SSD\"; module[4] = \"NETWORK\";"));
    assertTrue(interpreter.interpret("module[2] = null;"));
    assertTrue(interpreter.interpret("module.length;"));
    assertTrue(
        interpreter.interpret(
            "int count = 0; for (String entry : module) { if (entry != null) { count++; } }"));
    assertTrue(interpreter.interpret("int[] pakete = {15, 40, 20, 60, 30};"));
    assertTrue(
        interpreter.interpret(
            "for (int i = 0; i < pakete.length; i++) { roboter.collect(); }"));
  }
}
