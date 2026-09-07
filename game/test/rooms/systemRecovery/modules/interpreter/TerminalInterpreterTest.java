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
  private static final CodeLine START = line("start\\s*\\(\\s*\\)");
  private static final CodeLine STEP = line("step\\s*\\(\\s*\\)");
  private static final CodeLine FINISH = line("finish\\s*\\(\\s*\\)");
  private static final CodeLine LOOP =
      line("for\\s*\\(\\s*int\\s+i\\s*=\\s*0\\s*;\\s*i\\s*<\\s*items\\s*"
          + "\\.\\s*length\\s*;\\s*i\\+\\+\\s*\\)\\s*\\{");
  private static final CodeLine CONDITION =
      line("if\\s*\\(\\s*items\\s*\\[\\s*i\\s*]\\s*!=\\s*null\\s*\\)\\s*\\{");
  private static final CodeLine ACTION = line("use\\s*\\(\\s*items\\s*\\[\\s*i\\s*]\\s*\\)");
  private static final String IDENTIFIER = "[a-zA-Z][a-zA-Z0-9]*";
  private static final CodeLine FLEXIBLE_LOOP =
      line("for\\s*\\(\\s*int\\s+(?<index>"
          + IDENTIFIER
          + ")\\s*=\\s*0\\s*;\\s*\\k<index>\\s*<\\s*(?<array>"
          + IDENTIFIER
          + ")\\s*\\.\\s*length\\s*;\\s*\\k<index>\\+\\+\\s*\\)\\s*\\{");
  private static final CodeLine FLEXIBLE_ACTION =
      line("use\\s*\\(\\s*(?<array>"
          + IDENTIFIER
          + ")\\s*\\[\\s*(?<index>"
          + IDENTIFIER
          + ")\\s*]\\s*\\)");

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

  /** Ordered requirements reject the same statements in the wrong order. */
  @Test
  public void orderedRequirementRejectsWrongOrder() {
    TerminalInterpreter.instance()
        .register(
            0,
            new TerminalCodeRequirement(
                new CodeLine[] {FIRST_ASSIGNMENT, SECOND_ASSIGNMENT}, true, null, null));

    assertFalse(TerminalInterpreter.instance().analyze("energie[1] = 80; energie[0] = 20;"));
    assertTrue(TerminalInterpreter.instance().analyze("energie[0] = 20; energie[1] = 80;"));
  }

  /** Code accepted in a previous state may remain before an ordered current state. */
  @Test
  public void previousStateCodeMayRemainBeforeOrderedCurrentState() {
    TerminalInterpreter.instance()
        .register(0, new TerminalCodeRequirement(new CodeLine[] {START}, null, null));
    TerminalInterpreter.instance()
        .register(
            1, new TerminalCodeRequirement(new CodeLine[] {STEP, FINISH}, true, null, null));

    assertTrue(TerminalInterpreter.instance().interpret("start();"));

    boolean result = TerminalInterpreter.instance().analyze("start(); step(); finish();");

    assertTrue(result);
  }

  /** Previous-state code must not be inserted between ordered current-state statements. */
  @Test
  public void previousStateCodeDoesNotBelongBetweenOrderedCurrentStateStatements() {
    TerminalInterpreter.instance()
        .register(0, new TerminalCodeRequirement(new CodeLine[] {START}, null, null));
    TerminalInterpreter.instance()
        .register(
            1, new TerminalCodeRequirement(new CodeLine[] {STEP, FINISH}, true, null, null));

    assertTrue(TerminalInterpreter.instance().interpret("start();"));

    boolean result = TerminalInterpreter.instance().analyze("step(); start(); finish();");

    assertFalse(result);
  }

  /** For-loop headers are treated as one statement despite their inner semicolons. */
  @Test
  public void forHeaderStaysOneStatement() {
    TerminalInterpreter.instance()
        .register(0, new TerminalCodeRequirement(new CodeLine[] {LOOP}, true, null, null));

    boolean result =
        TerminalInterpreter.instance()
            .analyze("for (int i = 0; i < items.length; i++) {\n}");

    assertTrue(result);
  }

  /** Comments are ignored while checking otherwise valid terminal code. */
  @Test
  public void lineCommentsAreIgnored() {
    TerminalInterpreter.instance()
        .register(0, new TerminalCodeRequirement(new CodeLine[] {START}, null, null));

    assertTrue(TerminalInterpreter.instance().analyze("// comment\nstart();"));
  }

  /** Ordered code inside a required block is accepted. */
  @Test
  public void orderedRequirementAcceptsStatementInsideRequiredBlock() {
    TerminalInterpreter.instance()
        .register(0, new TerminalCodeRequirement(new CodeLine[] {LOOP, ACTION}, true, null, null));

    boolean result =
        TerminalInterpreter.instance()
            .analyze(
                """
                for (int i = 0; i < items.length; i++) {
                    use(items[i]);
                }
                """);

    assertTrue(result);
  }

  /** Ordered code after a closed required block is rejected. */
  @Test
  public void orderedRequirementRejectsStatementOutsideRequiredBlock() {
    TerminalInterpreter.instance()
        .register(0, new TerminalCodeRequirement(new CodeLine[] {LOOP, ACTION}, true, null, null));

    boolean result =
        TerminalInterpreter.instance()
            .analyze(
                """
                for (int i = 0; i < items.length; i++) {
                }
                use(items[i]);
                """);

    assertFalse(result);
  }

  /** Ordered code inside nested required blocks is accepted. */
  @Test
  public void orderedRequirementAcceptsStatementInsideNestedRequiredBlock() {
    TerminalInterpreter.instance()
        .register(
            0,
            new TerminalCodeRequirement(new CodeLine[] {LOOP, CONDITION, ACTION}, true, null, null));

    boolean result =
        TerminalInterpreter.instance()
            .analyze(
                """
                for (int i = 0; i < items.length; i++) {
                    if (items[i] != null) {
                        use(items[i]);
                    }
                }
                """);

    assertTrue(result);
  }

  /** Ordered code outside the innermost required block is rejected. */
  @Test
  public void orderedRequirementRejectsStatementOutsideNestedRequiredBlock() {
    TerminalInterpreter.instance()
        .register(
            0,
            new TerminalCodeRequirement(new CodeLine[] {LOOP, CONDITION, ACTION}, true, null, null));

    boolean result =
        TerminalInterpreter.instance()
            .analyze(
                """
                for (int i = 0; i < items.length; i++) {
                    if (items[i] != null) {
                    }
                    use(items[i]);
                }
                """);

    assertFalse(result);
  }

  /** Captured names may vary as long as later statements reuse the same names. */
  @Test
  public void sharedCapturesAcceptConsistentFlexibleNames() {
    TerminalInterpreter.instance()
        .register(
            0,
            new TerminalCodeRequirement(
                new CodeLine[] {FLEXIBLE_LOOP, FLEXIBLE_ACTION}, true, null, null));

    assertTrue(
        TerminalInterpreter.instance()
            .analyze(
                """
                for (int index = 0; index < modules.length; index++) {
                    use(modules[index]);
                }
                """));
    assertTrue(
        TerminalInterpreter.instance()
            .analyze(
                """
                for (int i = 0; i < items.length; i++) {
                    use(items[i]);
                }
                """));
  }

  /** Captured array names must be reused by later statements. */
  @Test
  public void sharedCapturesRejectDifferentArrayName() {
    TerminalInterpreter.instance()
        .register(
            0,
            new TerminalCodeRequirement(
                new CodeLine[] {FLEXIBLE_LOOP, FLEXIBLE_ACTION}, true, null, null));

    boolean result =
        TerminalInterpreter.instance()
            .analyze(
                """
                for (int index = 0; index < modules.length; index++) {
                    use(items[index]);
                }
                """);

    assertFalse(result);
  }

  /** Captured loop index names must be reused by later statements. */
  @Test
  public void sharedCapturesRejectDifferentIndexName() {
    TerminalInterpreter.instance()
        .register(
            0,
            new TerminalCodeRequirement(
                new CodeLine[] {FLEXIBLE_LOOP, FLEXIBLE_ACTION}, true, null, null));

    boolean result =
        TerminalInterpreter.instance()
            .analyze(
                """
                for (int index = 0; index < modules.length; index++) {
                    use(modules[i]);
                }
                """);

    assertFalse(result);
  }

  /** Back references keep the chosen index variable consistent inside a single loop header. */
  @Test
  public void namedBackReferenceRejectsInconsistentLoopHeader() {
    TerminalInterpreter.instance()
        .register(0, new TerminalCodeRequirement(new CodeLine[] {FLEXIBLE_LOOP}, true, null, null));

    boolean result =
        TerminalInterpreter.instance()
            .analyze("for (int index = 0; i < modules.length; index++) {}");

    assertFalse(result);
  }

  /** Shared captures also work when an unordered requirement accepts statements in any order. */
  @Test
  public void sharedCapturesWorkForUnorderedRequirements() {
    TerminalInterpreter.instance()
        .register(
            0,
            new TerminalCodeRequirement(
                new CodeLine[] {FLEXIBLE_LOOP, FLEXIBLE_ACTION}, null, null));

    boolean result =
        TerminalInterpreter.instance()
            .analyze(
                """
                use(modules[index]);
                for (int index = 0; index < modules.length; index++) {
                }
                """);

    assertTrue(result);
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

  private static CodeLine line(String regex) {
    return new CodeLine(Pattern.compile(regex));
  }
}
