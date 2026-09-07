package rooms.systemRecovery.modules.interpreter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the generic terminal command interpreter. */
public class TerminalInterpreterTest {

  private static final CodeLine INITIALIZE =
      new CodeLine(
          Pattern.compile("int\\s*\\[\\s*]\\s*energie\\s*=\\s*new\\s+int\\s*\\[\\s*5\\s*]"),
          Pattern.compile("int\\s+energie\\s*\\[\\s*]\\s*=\\s*new\\s+int\\s*\\[\\s*5\\s*]"));
  private static final CodeLine FIRST_ASSIGNMENT =
      new CodeLine(Pattern.compile("energie\\s*\\[\\s*0\\s*]\\s*=\\s*20"));
  private static final CodeLine SECOND_ASSIGNMENT =
      new CodeLine(Pattern.compile("energie\\s*\\[\\s*1\\s*]\\s*=\\s*80"));
  private static final CodeLine THIRD_ASSIGNMENT =
      new CodeLine(Pattern.compile("energie\\s*\\[\\s*2\\s*]\\s*=\\s*50"));

  /** Resets and registers isolated steps before each test. */
  @BeforeEach
  public void setup() {
    TerminalInterpreter.instance().reset();
    TerminalInterpreter.instance()
        .register(0, new TerminalCodeRequirement(new CodeLine[] {INITIALIZE}, null, null));
    TerminalInterpreter.instance()
        .register(
            1,
            new TerminalCodeRequirement(
                new CodeLine[] {FIRST_ASSIGNMENT, SECOND_ASSIGNMENT}, null, null));
  }

  /** Regex commands accept flexible whitespace but still validate the complete statement. */
  @Test
  public void regexSyntaxMatchesCompleteStatement() {
    assertTrue(INITIALIZE.check("int [ ] energie = new int [ 5 ]"));
    assertTrue(INITIALIZE.check("int energie[] = new int[5]"));
    assertFalse(INITIALIZE.check("prefix int[] energie = new int[5]"));
  }

  /** Every permutation of the required code lines completes an unordered requirement. */
  @Test
  public void codeLineOrderDoesNotAffectResult() {
    TerminalInterpreter.instance()
        .register(
            0,
            new TerminalCodeRequirement(
                new CodeLine[] {FIRST_ASSIGNMENT, SECOND_ASSIGNMENT, THIRD_ASSIGNMENT},
                null,
                null));

    String[] permutations = {
      "energie[0] = 20; energie[1] = 80; energie[2] = 50;",
      "energie[0] = 20; energie[2] = 50; energie[1] = 80;",
      "energie[1] = 80; energie[0] = 20; energie[2] = 50;",
      "energie[1] = 80; energie[2] = 50; energie[0] = 20;",
      "energie[2] = 50; energie[0] = 20; energie[1] = 80;",
      "energie[2] = 50; energie[1] = 80; energie[0] = 20;"
    };

    for (String source : permutations) {
      assertTrue(TerminalInterpreter.instance().analyze(source), source);
    }
  }

  /** Code accepted in a previous state may remain in the editor. */
  @Test
  public void previousStateCodeMayRemainInSource() {
    assertTrue(TerminalInterpreter.instance().interpret("int[] energie = new int[5];"));

    boolean result =
        TerminalInterpreter.instance()
            .analyze("int[] energie = new int[5];\nenergie[0]=20;;\nenergie [ 1 ] = 80;");

    assertTrue(result);
  }

  /** Partial code is rejected and can be completed because failure does not advance the state. */
  @Test
  public void partialCodeIsRejectedAndCanBeCompleted() {
    TerminalInterpreter.instance().interpret("int[] energie = new int[5];");

    boolean partialResult = TerminalInterpreter.instance().interpret("energie[0] = 20;");
    boolean completeResult =
        TerminalInterpreter.instance().interpret("energie[0] = 20; energie[1] = 80;");

    assertFalse(partialResult);
    assertTrue(completeResult);
  }
}
