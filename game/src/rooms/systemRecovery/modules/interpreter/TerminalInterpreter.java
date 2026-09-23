package rooms.systemRecovery.modules.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Interprets registered terminal puzzle states without knowing room-specific behavior. */
public final class TerminalInterpreter {

  private static final TerminalInterpreter INSTANCE = new TerminalInterpreter();

  private final Map<Integer, TerminalCodeRequirement> states = new HashMap<>();
  private final TerminalMatchContext successfulContext = new TerminalMatchContext();
  private final List<AcceptedInput> acceptedInputs = new ArrayList<>();
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
    acceptedInputs.clear();
  }

  /**
   * Returns the accepted source history needed to reconstruct flexible variable names after a
   * checkpoint load.
   *
   * @return immutable copy in acceptance order
   */
  public List<AcceptedInput> acceptedInputs() {
    return Collections.unmodifiableList(new ArrayList<>(acceptedInputs));
  }

  /**
   * Restores accepted terminal input without invoking room callbacks, tracking, sounds or UI.
   *
   * <p>Every source is revalidated against the same parser and capture context used during normal
   * play. A malformed or out-of-order history is rejected as one operation and leaves the
   * interpreter reset, preventing a partially restored state.
   *
   * @param inputs accepted source history in original order
   * @throws IllegalArgumentException if the history cannot be replayed
   */
  public void restoreAcceptedInputs(List<AcceptedInput> inputs) {
    reset();
    if (inputs == null) {
      throw new IllegalArgumentException("Accepted input history must not be null.");
    }
    for (AcceptedInput input : inputs) {
      if (input == null || input.state() != currentState || input.source() == null) {
        reset();
        throw new IllegalArgumentException("Accepted input history is not sequential.");
      }
      AnalysisResult result = analysis(input.state(), input.source(), successfulContext.copy());
      if (!result.successful()) {
        reset();
        throw new IllegalArgumentException(
            "Accepted input history contains invalid source for state " + input.state());
      }
      successfulContext.replaceWith(result.context());
      acceptedInputs.add(input);
      currentState++;
    }
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
   * Applies a state received from the authoritative multiplayer server.
   *
   * @param state synchronized interpreter state
   */
  public void synchronizeState(int state) {
    currentState = Math.max(0, state);
  }

  /**
   * Checks the source, invokes the matching callback, and advances after success.
   *
   * @param source source text entered in the terminal
   * @return true if the complete current state is correct
   */
  public boolean interpret(String source) {
    return interpret(source, -1);
  }

  /**
   * Checks source and passes explicit player context to the registered callback.
   *
   * @param source source text entered in the terminal
   * @param playerId authoritative player ID, or {@code -1} for a non-player call
   * @return whether the complete current state is correct
   */
  public boolean interpret(String source, int playerId) {
    return interpret(source, playerId, null);
  }

  /**
   * Checks source and keeps the originating dialog available to room-side callbacks.
   *
   * @param source source text entered in the terminal
   * @param playerId authoritative player ID, or {@code -1} for a non-player call
   * @param dialogId dialog that submitted the source, or {@code null}
   * @return whether the complete current state is correct
   */
  public boolean interpret(String source, int playerId, String dialogId) {
    TerminalCodeRequirement puzzleState = states.get(currentState);
    if (puzzleState == null) {
      return false;
    }

    TerminalAttempt attempt = new TerminalAttempt(currentState, source, playerId, dialogId);
    AnalysisResult result = analysis(source, successfulContext.copy());
    boolean successful = result.successful();
    if (successful) {
      // Apply the room effect before committing the shared interpreter state. If the room-side
      // effect fails, the step remains retryable instead of looking accepted to the player.
      puzzleState.onSuccess().accept(attempt);
      successfulContext.replaceWith(result.context());
      acceptedInputs.add(new AcceptedInput(currentState, source));
      currentState++;
    } else {
      puzzleState.onFailure().accept(attempt);
    }
    return successful;
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

  /**
   * Checks a standalone requirement without registering it as a shared terminal state.
   *
   * <p>This is used for code stored on physical programming items. The requirement gets a fresh
   * capture context and only its own statements are allowed, so a previous terminal state cannot
   * accidentally make chip code valid.
   *
   * @param requirement code requirement to validate
   * @param source source text to validate
   * @return whether the source completely satisfies the standalone requirement
   */
  public boolean analyze(TerminalCodeRequirement requirement, String source) {
    if (requirement == null) {
      return false;
    }
    TerminalMatchContext context = new TerminalMatchContext();
    List<TerminalStatement> statements = parsedStatements(source);
    return !statements.isEmpty()
        && matchesRequiredCodeLines(statements, requirement, context)
        && containsOnlyRequiredStatements(statements, requirement, context);
  }

  /**
   * Checks a registered state without changing the current state or invoking callbacks.
   *
   * <p>This is used by physical programming items whose source is validated before the shared
   * terminal state is allowed to advance.
   *
   * @param state state to validate
   * @param source source text to validate
   * @return whether the source completely satisfies the requested state
   */
  public boolean analyzeState(int state, String source) {
    return analysis(state, source, successfulContext.copy()).successful();
  }

  private AnalysisResult analysis(String source, TerminalMatchContext context) {
    return analysis(currentState, source, context);
  }

  private AnalysisResult analysis(int state, String source, TerminalMatchContext context) {
    TerminalCodeRequirement puzzleState = states.get(state);
    if (puzzleState == null) {
      return new AnalysisResult(false, context);
    }
    List<TerminalStatement> statements = parsedStatements(source);
    boolean successful =
        !statements.isEmpty()
            && matchesRequiredCodeLines(statements, puzzleState, context)
            && containsOnlyKnownStatements(statements, state, context);
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
      List<TerminalStatement> statements, int state, TerminalMatchContext context) {
    for (TerminalStatement statement : statements) {
      if (!matchesStateUpTo(statement.source(), state, context)) {
        return false;
      }
    }
    return true;
  }

  private static boolean containsOnlyRequiredStatements(
      List<TerminalStatement> statements,
      TerminalCodeRequirement requirement,
      TerminalMatchContext context) {
    for (TerminalStatement statement : statements) {
      boolean known = false;
      for (CodeLine codeLine : requirement.codeLines()) {
        TerminalMatchContext candidate = context.copy();
        if (codeLine.check(statement.source(), candidate)) {
          context.replaceWith(candidate);
          known = true;
          break;
        }
      }
      if (!known) {
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
   * @param state highest state whose requirements may be reused
   * @param context already known named captures used to keep flexible variable names consistent
   * @return whether the statement matches a requirement up to the requested state
   */
  private boolean matchesStateUpTo(String statement, int state, TerminalMatchContext context) {
    for (Map.Entry<Integer, TerminalCodeRequirement> entry : states.entrySet()) {
      if (entry.getKey() > state) {
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

  /**
   * One source accepted by the shared interpreter at a stable state ID.
   *
   * @param state interpreter state that accepted the source
   * @param source exact source submitted by the player
   */
  public record AcceptedInput(int state, String source) {}
}
