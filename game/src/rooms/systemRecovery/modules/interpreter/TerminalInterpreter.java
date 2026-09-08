package rooms.systemRecovery.modules.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Interprets registered terminal puzzle states without knowing room-specific behavior. */
public final class TerminalInterpreter {

  private static final TerminalInterpreter INSTANCE = new TerminalInterpreter();

  private final Map<Integer, TerminalCodeRequirement> states = new HashMap<>();
  private final TerminalMatchContext successfulContext = new TerminalMatchContext();
  private int currentState;

  private TerminalInterpreter() {}

  /**
   * Returns the shared terminal interpreter.
   *
   * @return singleton interpreter instance
   */
  public static TerminalInterpreter instance() {
    return INSTANCE;
  }

  /**
   * Registers one terminal puzzle state.
   *
   * @param state interpreter state
   * @param puzzleState patterns and callbacks for the state
   */
  public void register(int state, TerminalCodeRequirement puzzleState) {
    states.put(state, puzzleState);
  }

  /** Resets the interpreter to the first puzzle state. */
  public void reset() {
    currentState = 0;
    successfulContext.clear();
  }

  /**
   * Returns the currently expected interpreter state.
   *
   * @return current interpreter state
   */
  public int currentState() {
    return currentState;
  }

  /**
   * Checks the source, invokes the matching callback, and advances after success.
   *
   * @param source source text entered in the terminal
   * @return true if the complete current state is correct
   */
  public boolean interpret(String source) {
    TerminalCodeRequirement puzzleState = states.get(currentState);
    if (puzzleState == null) {
      return false;
    }

    AnalysisResult result = analysis(source, successfulContext.copy());
    boolean successful = result.successful();
    if (successful) {
      successfulContext.replaceWith(result.context());
      currentState++;
      // Room-side feedback must not be able to block the already validated state transition.
      puzzleState.onSuccess().run();
    } else {
      puzzleState.onFailure().run();
    }
    return successful;
  }

  /**
   * Completes the current state without source validation for local debug sessions.
   *
   * @return true if a state was completed
   */
  public boolean advanceCurrentStateForDebug() {
    TerminalCodeRequirement puzzleState = states.get(currentState);
    if (puzzleState == null) {
      return false;
    }

    currentState++;
    puzzleState.onSuccess().run();
    return true;
  }

  /**
   * Checks the source without invoking callbacks or changing state.
   *
   * @param source source text entered in the terminal
   * @return true if the complete current state is correct
   */
  public boolean analyze(String source) {
    return analysis(source, successfulContext.copy()).successful();
  }

  private AnalysisResult analysis(String source, TerminalMatchContext context) {
    TerminalCodeRequirement puzzleState = states.get(currentState);
    if (puzzleState == null) {
      return new AnalysisResult(false, context);
    }
    List<TerminalStatement> statements = parsedStatements(source);
    boolean successful =
        !statements.isEmpty()
            && matchesRequiredCodeLines(statements, puzzleState, context)
            && containsOnlyKnownStatements(statements, context);
    return new AnalysisResult(successful, context);
  }

  private static boolean matchesRequiredCodeLines(
      List<TerminalStatement> statements,
      TerminalCodeRequirement puzzleState,
      TerminalMatchContext context) {
    if (puzzleState.requiresOrder()) {
      return containsCodeLinesInOrder(statements, puzzleState.codeLines(), context);
    }
    return containsEveryCodeLine(statements, puzzleState.codeLines(), context);
  }

  private static boolean containsEveryCodeLine(
      List<TerminalStatement> statements, CodeLine[] codeLines, TerminalMatchContext context) {
    for (CodeLine codeLine : codeLines) {
      if (!containsCodeLine(statements, codeLine, context)) {
        return false;
      }
    }
    return true;
  }

  private static boolean containsCodeLine(
      List<TerminalStatement> statements, CodeLine codeLine, TerminalMatchContext context) {
    for (TerminalStatement statement : statements) {
      TerminalMatchContext candidate = context.copy();
      if (codeLine.check(statement.source(), candidate)) {
        context.replaceWith(candidate);
        return true;
      }
    }
    return false;
  }

  private static boolean containsCodeLinesInOrder(
      List<TerminalStatement> statements, CodeLine[] codeLines, TerminalMatchContext context) {
    for (int startIndex = 0; startIndex < statements.size(); startIndex++) {
      TerminalMatchContext candidate = context.copy();
      if (containsCodeLinesInOrderFrom(statements, codeLines, startIndex, candidate)) {
        context.replaceWith(candidate);
        return true;
      }
    }
    return false;
  }

  private static boolean containsCodeLinesInOrderFrom(
      List<TerminalStatement> statements,
      CodeLine[] codeLines,
      int startIndex,
      TerminalMatchContext context) {
    int nextCodeLineIndex = 0;
    int minimumBlockDepth = 0;
    for (int statementIndex = startIndex; statementIndex < statements.size(); statementIndex++) {
      TerminalStatement statement = statements.get(statementIndex);
      if (statement.blockDepth() < minimumBlockDepth) {
        continue;
      }
      TerminalMatchContext candidate = context.copy();
      if (codeLines[nextCodeLineIndex].check(statement.source(), candidate)) {
        context.replaceWith(candidate);
        if (opensControlBlock(statement.source())) {
          minimumBlockDepth = statement.blockDepth() + 1;
        }
        nextCodeLineIndex++;
      } else if (nextCodeLineIndex > 0) {
        return false;
      }
      if (nextCodeLineIndex == codeLines.length) {
        return true;
      }
    }
    return false;
  }

  private boolean containsOnlyKnownStatements(
      List<TerminalStatement> statements, TerminalMatchContext context) {
    for (TerminalStatement statement : statements) {
      if (!matchesStateUpToCurrent(statement.source(), context)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether a statement belongs to the current or an already completed state.
   *
   * <p>This allows code from previous states to remain in the editor. As a consequence, an
   * identical code line required by a later state is already considered present and can satisfy
   * that later requirement without being entered again. Puzzle definitions must account for this
   * limitation.
   *
   * @param statement statement to check
   * @param context already known named captures used to keep flexible variable names consistent
   * @return whether the statement matches a requirement up to the current state
   */
  private boolean matchesStateUpToCurrent(String statement, TerminalMatchContext context) {
    for (Map.Entry<Integer, TerminalCodeRequirement> entry : states.entrySet()) {
      if (entry.getKey() > currentState) {
        continue;
      }
      for (CodeLine codeLine : entry.getValue().codeLines()) {
        if (codeLine.check(statement, context.copy())) {
          return true;
        }
      }
    }
    return false;
  }

  private static String[] statements(String source) {
    return parsedStatements(source).stream().map(TerminalStatement::source).toArray(String[]::new);
  }

  private static List<TerminalStatement> parsedStatements(String source) {
    List<TerminalStatement> statements = new ArrayList<>();
    StringBuilder currentStatement = new StringBuilder();
    String sourceWithoutComments = removeLineComments(source);
    int parenthesesDepth = 0;
    int blockDepth = 0;

    for (int index = 0; index < sourceWithoutComments.length(); index++) {
      char character = sourceWithoutComments.charAt(index);
      if (character == '}' && currentStatement.toString().trim().isBlank()) {
        currentStatement.setLength(0);
        blockDepth = Math.max(0, blockDepth - 1);
        continue;
      }
      currentStatement.append(character);
      parenthesesDepth = updatedParenthesesDepth(parenthesesDepth, character);
      if (isStatementEnd(currentStatement, character, parenthesesDepth)) {
        addStatement(statements, currentStatement, blockDepth);
        if (opensControlBlock(statements.get(statements.size() - 1).source())) {
          blockDepth++;
        }
      }
    }
    addStatement(statements, currentStatement, blockDepth);
    return statements;
  }

  private static int updatedParenthesesDepth(int parenthesesDepth, char character) {
    if (character == '(') {
      return parenthesesDepth + 1;
    }
    if (character == ')') {
      return Math.max(0, parenthesesDepth - 1);
    }
    return parenthesesDepth;
  }

  private static boolean isStatementEnd(
      StringBuilder currentStatement, char character, int parenthesesDepth) {
    return (character == ';' && parenthesesDepth == 0)
        || isControlBlockStart(currentStatement, character);
  }

  private static boolean isControlBlockStart(StringBuilder currentStatement, char character) {
    if (character != '{') {
      return false;
    }
    String statement = currentStatement.toString().trim();
    return opensControlBlock(statement);
  }

  private static boolean opensControlBlock(String statement) {
    return statement.matches("(for|if)\\s*\\(.*\\{");
  }

  private static void addStatement(
      List<TerminalStatement> statements, StringBuilder currentStatement, int blockDepth) {
    String statement = normalizedStatement(currentStatement);
    if (!statement.isBlank()) {
      statements.add(new TerminalStatement(statement, blockDepth));
    }
    currentStatement.setLength(0);
  }

  private static String normalizedStatement(StringBuilder currentStatement) {
    String statement = currentStatement.toString().trim();
    while (statement.endsWith(";")) {
      statement = statement.substring(0, statement.length() - 1).trim();
    }
    while (statement.startsWith("}")) {
      statement = statement.substring(1).trim();
    }
    return statement;
  }

  private static String removeLineComments(String source) {
    return Arrays.stream(source.split("\\R", -1))
        .map(TerminalInterpreter::removeLineComment)
        .collect(Collectors.joining("\n"));
  }

  private static String removeLineComment(String line) {
    int commentStart = line.indexOf("//");
    if (commentStart < 0) {
      return line;
    }
    return line.substring(0, commentStart);
  }

  private record TerminalStatement(String source, int blockDepth) {}

  private record AnalysisResult(boolean successful, TerminalMatchContext context) {}
}
