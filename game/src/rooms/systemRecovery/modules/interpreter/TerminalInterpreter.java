package rooms.systemRecovery.modules.interpreter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Interprets registered terminal puzzle states without knowing room-specific behavior. */
public final class TerminalInterpreter {

  private static final TerminalInterpreter INSTANCE = new TerminalInterpreter();

  private final Map<Integer, TerminalCodeRequirement> states = new HashMap<>();
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

    boolean successful = analyze(source);
    if (successful) {
      puzzleState.onSuccess().run();
      currentState++;
    } else {
      puzzleState.onFailure().run();
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
    TerminalCodeRequirement puzzleState = states.get(currentState);
    if (puzzleState == null) {
      return false;
    }
    String[] statements = statements(source);
    return statements.length > 0
        && containsEveryCodeLine(statements, puzzleState.codeLines())
        && containsOnlyKnownStatements(statements);
  }

  private static boolean containsEveryCodeLine(String[] statements, CodeLine[] codeLines) {
    for (CodeLine codeLine : codeLines) {
      if (Arrays.stream(statements).noneMatch(codeLine::check)) {
        return false;
      }
    }
    return true;
  }

  private boolean containsOnlyKnownStatements(String[] statements) {
    for (String statement : statements) {
      if (!matchesStateUpToCurrent(statement)) {
        return false;
      }
    }
    return true;
  }

  private boolean matchesStateUpToCurrent(String statement) {
    for (Map.Entry<Integer, TerminalCodeRequirement> entry : states.entrySet()) {
      if (entry.getKey() > currentState) {
        continue;
      }
      for (CodeLine codeLine : entry.getValue().codeLines()) {
        if (codeLine.check(statement)) {
          return true;
        }
      }
    }
    return false;
  }

  private static String[] statements(String source) {
    return Arrays.stream(source.split(";"))
        .map(String::trim)
        .filter(statement -> !statement.isBlank())
        .toArray(String[]::new);
  }
}
