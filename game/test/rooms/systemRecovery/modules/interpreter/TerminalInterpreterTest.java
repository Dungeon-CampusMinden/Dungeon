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

  /** A multi-command state is order independent and ignores prior successful commands. */
  @Test
  public void multiCommandStepIsOrderIndependent() {
    assertTrue(TerminalInterpreter.instance().interpret("int[] energie = new int[5];"));

    boolean result =
        TerminalInterpreter.instance()
            .analyze("int[] energie = new int[5];\nenergie[1]=80;;\nenergie [ 0 ] = 20;");

    assertTrue(result);
  }

  /** Removing a previously correct current-state command makes the state incomplete again. */
  @Test
  public void missingCommandDoesNotAdvanceState() {
    TerminalInterpreter.instance().interpret("int[] energie = new int[5];");

    boolean result = TerminalInterpreter.instance().interpret("energie[0] = 20;");

    assertFalse(result);
  }
}
