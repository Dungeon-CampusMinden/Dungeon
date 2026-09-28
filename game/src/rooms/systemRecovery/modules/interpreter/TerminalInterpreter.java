package rooms.systemRecovery.modules.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import rooms.systemRecovery.util.interpreter.TerminalStep;

/** Interprets registered terminal puzzle states without knowing room-specific behavior. */
public final class TerminalInterpreter {

  private static final TerminalInterpreter INSTANCE = new TerminalInterpreter();
  private static final Pattern CONTROL_BLOCK_HEADER =
      Pattern.compile("(for|if)\\s*\\(.*\\{", Pattern.DOTALL);
  private static final String IDENTIFIER_PATTERN = "[a-zA-Z][a-zA-Z0-9]*";
  private static final String ARRAY_REFERENCE_PATTERN =
      IDENTIFIER_PATTERN + "(?:\\s*\\[\\s*" + IDENTIFIER_PATTERN + "\\s*])?";
  private static final Pattern LENGTH_INITIALIZER =
      Pattern.compile(
          "int\\s+(?<name>" + IDENTIFIER_PATTERN + ")\\s*=\\s*(?<array>"
              + ARRAY_REFERENCE_PATTERN
              + ")\\s*\\.\\s*length\\s*;");
  private static final Pattern ZERO_INITIALIZER =
      Pattern.compile("int\\s+(?<name>" + IDENTIFIER_PATTERN + ")\\s*=\\s*0\\s*;");
  private static final Pattern LENGTH_ASSIGNMENT =
      Pattern.compile(
          "(?<name>" + IDENTIFIER_PATTERN + ")\\s*=\\s*(?<array>"
              + ARRAY_REFERENCE_PATTERN
              + ")\\s*\\.\\s*length\\s*;");
  private static final Pattern VARIABLE_ASSIGNMENT =
      Pattern.compile("(?<name>" + IDENTIFIER_PATTERN + ")\\s*(?:=(?!=)|\\+=|-=|\\+\\+|--)");

  private final Map<Integer, TerminalCodeRequirement> states = new HashMap<>();
  private final Map<Integer, boolean[]> partialMatches = new HashMap<>();
  private final Map<Integer, List<String>> partialSources = new HashMap<>();
  private final Map<Integer, TerminalMatchContext> partialBaseContexts = new HashMap<>();
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
    partialMatches.clear();
    partialSources.clear();
    partialBaseContexts.clear();
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
    restoreAcceptedInputs(inputs, -1);
  }

  /**
   * Restores accepted terminal input and positions the interpreter at a validated checkpoint.
   * Non-terminal editor/form states may be crossed, but a missing ordinary terminal input is
   * rejected.
   *
   * @param inputs accepted source history in original order
   * @param targetState checkpoint state, or a negative value to stop after the last accepted input
   * @throws IllegalArgumentException if the history or target skips an ordinary terminal state
   */
  public void restoreAcceptedInputs(List<AcceptedInput> inputs, int targetState) {
    reset();
    if (inputs == null) {
      throw new IllegalArgumentException("Accepted input history must not be null.");
    }
    int expectedState = 0;
    for (AcceptedInput input : inputs) {
      if (input == null || input.source() == null) {
        reset();
        throw new IllegalArgumentException("Accepted input history contains an invalid entry.");
      }
      expectedState = skipNonTerminalStates(expectedState, input.state());
      if (input.state() != expectedState) {
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
      expectedState++;
    }

    if (targetState >= 0) {
      expectedState = skipNonTerminalStates(expectedState, targetState);
      if (expectedState != targetState) {
        reset();
        throw new IllegalArgumentException("Checkpoint skips a required terminal state.");
      }
      currentState = targetState;
    } else {
      currentState = expectedState;
    }
  }

  private static int skipNonTerminalStates(int state, int targetState) {
    int next = state;
    while (next < targetState) {
      TerminalStep step = TerminalStep.fromStateId(next).orElse(null);
      if (step == null || step.inputMode() == TerminalStep.InputMode.TERMINAL) break;
      next++;
    }
    return next;
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
    int synchronizedState = Math.max(0, state);
    if (currentState != synchronizedState) {
      partialMatches.clear();
      partialSources.clear();
      partialBaseContexts.clear();
    }
    currentState = synchronizedState;
  }

  /**
   * Checks the source, invokes the matching callback, and advances after success.
   *
   * @param source source text entered in the terminal
   * @return true if the current state is complete or a valid partial input was accepted
   */
  public boolean interpret(String source) {
    return interpret(source, -1);
  }

  /**
   * Checks source and passes explicit player context to the registered callback.
   *
   * @param source source text entered in the terminal
   * @param playerId authoritative player ID, or {@code -1} for a non-player call
   * @return whether the source was accepted, including a valid partial input
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
   * @return whether the source was accepted, including a valid partial input
   */
  public boolean interpret(String source, int playerId, String dialogId) {
    if (!states.containsKey(currentState)) {
      return false;
    }

    boolean accepted = false;
    boolean combinedSubmission = false;
    while (true) {
      int state = currentState;
      TerminalCodeRequirement requirement = states.get(state);
      if (requirement == null) return accepted;

      int maxKnownState = state;
      if (requirement.acceptsFollowingStepInSameSubmission()) maxKnownState++;
      StateEvaluation evaluation =
          evaluateState(state, source, successfulContext.copy(), maxKnownState);

      if (evaluation.status() == EvaluationStatus.COMPLETE) {
        boolean completesFollowingStep =
            requirement.acceptsFollowingStepInSameSubmission()
                && evaluateState(
                        state + 1, source, evaluation.context().copy(), state + 1)
                    .status()
                    == EvaluationStatus.COMPLETE;
        combinedSubmission |= completesFollowingStep;
        TerminalAttempt attempt =
            new TerminalAttempt(
                state,
                source,
                playerId,
                dialogId,
                combinedSubmission,
                evaluation.completeSubmission()
                    ? source
                    : acceptedSourceForStep(state, source));

        // Apply room effects before committing this state so a failed callback stays retryable.
        requirement.onSuccess().accept(attempt);
        successfulContext.replaceWith(evaluation.context());
        acceptedInputs.add(new AcceptedInput(state, attempt.acceptedSource()));
        partialMatches.remove(state);
        partialSources.remove(state);
        partialBaseContexts.remove(state);
        currentState++;
        accepted = true;

        if (!requirement.acceptsFollowingStepInSameSubmission()) return true;
        continue;
      }

      if (evaluation.status() == EvaluationStatus.PARTIAL) {
        partialBaseContexts.putIfAbsent(state, successfulContext.copy());
        partialMatches.put(state, evaluation.matchedLines());
        partialSources.computeIfAbsent(state, ignored -> new ArrayList<>()).add(source);
        successfulContext.replaceWith(evaluation.context());
        requirement
            .onPartialInput()
            .accept(new TerminalAttempt(state, source, playerId, dialogId, false));
        return true;
      }

      if (evaluation.status() == EvaluationStatus.NO_PROGRESS && accepted) return true;

      requirement.onFailure().accept(new TerminalAttempt(state, source, playerId, dialogId));
      return false;
    }
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
    List<TerminalStatement> statements = matchingStatements(source);
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
    int maxKnownState =
        puzzleState.acceptsFollowingStepInSameSubmission() ? state + 1 : state;
    return analysis(state, source, context, maxKnownState);
  }

  private AnalysisResult analysis(
      int state, String source, TerminalMatchContext context, int maxKnownState) {
    TerminalCodeRequirement puzzleState = states.get(state);
    if (puzzleState == null) return new AnalysisResult(false, context);
    List<TerminalStatement> statements = matchingStatements(source);
    boolean successful =
        !statements.isEmpty()
            && matchesRequiredCodeLines(statements, puzzleState, context)
            && containsOnlyKnownStatements(statements, maxKnownState, context);
    return new AnalysisResult(successful, context);
  }

  private StateEvaluation evaluateState(
      int state, String source, TerminalMatchContext context, int maxKnownState) {
    TerminalCodeRequirement requirement = states.get(state);
    if (requirement == null) {
      return new StateEvaluation(EvaluationStatus.INVALID, context, new boolean[0], false);
    }

    if (!requirement.acceptsPartialInput()) {
      AnalysisResult result = analysis(state, source, context, maxKnownState);
      return new StateEvaluation(
          result.successful() ? EvaluationStatus.COMPLETE : EvaluationStatus.INVALID,
          result.context(),
          new boolean[0],
          result.successful());
    }

    return evaluatePartialState(state, source, context, maxKnownState, requirement);
  }

  private StateEvaluation evaluatePartialState(
      int state,
      String source,
      TerminalMatchContext context,
      int maxKnownState,
      TerminalCodeRequirement requirement) {
    List<TerminalStatement> statements = matchingStatements(source);
    CodeLine[] requiredLines = requirement.codeLines();
    boolean[] matchedLines =
        Arrays.copyOf(
            partialMatches.getOrDefault(state, new boolean[requiredLines.length]),
            requiredLines.length);

    TerminalMatchContext baseContext =
        partialBaseContexts.getOrDefault(state, context).copy();
    AnalysisResult completeSubmission =
        analysis(state, source, baseContext, maxKnownState);
    if (completeSubmission.successful()) {
      return new StateEvaluation(
          EvaluationStatus.COMPLETE,
          completeSubmission.context(),
          matchedLines,
          true);
    }

    boolean matchedCurrentStep = false;

    for (TerminalStatement statement : statements) {
      boolean matchedStatement = false;
      for (int lineIndex = 0; lineIndex < requiredLines.length; lineIndex++) {
        if (matchedLines[lineIndex]
            || !requirement.prerequisitesMet(lineIndex, matchedLines)) {
          continue;
        }
        TerminalMatchContext candidate = context.copy();
        if (requiredLines[lineIndex].check(statement.source(), candidate)
            && requirement.captureGroupsAreDistinct(candidate)) {
          matchedLines[lineIndex] = true;
          matchedCurrentStep = true;
          matchedStatement = true;
          context.replaceWith(candidate);
        }
      }
      if (!matchedStatement && !matchesStateUpTo(statement.source(), maxKnownState, context)) {
        return new StateEvaluation(EvaluationStatus.INVALID, context, matchedLines, false);
      }
    }

    if (statements.isEmpty()) {
      return new StateEvaluation(EvaluationStatus.NO_PROGRESS, context, matchedLines, false);
    }
    if (allMatched(matchedLines)) {
      return new StateEvaluation(EvaluationStatus.COMPLETE, context, matchedLines, false);
    }
    return new StateEvaluation(
        matchedCurrentStep ? EvaluationStatus.PARTIAL : EvaluationStatus.NO_PROGRESS,
        context,
        matchedLines,
        false);
  }

  private static boolean allMatched(boolean[] matches) {
    for (boolean matched : matches) {
      if (!matched) return false;
    }
    return matches.length > 0;
  }

  private String acceptedSourceForStep(int state, String completingSource) {
    List<String> acceptedParts = partialSources.get(state);
    if (acceptedParts == null || acceptedParts.isEmpty()) return completingSource;
    if (acceptedParts.stream().allMatch(part -> containsStatements(completingSource, part))) {
      return completingSource;
    }

    List<String> completeSource = new ArrayList<>(acceptedParts);
    completeSource.add(completingSource);
    return String.join("\n", completeSource);
  }

  private static boolean containsStatements(String source, String expectedSource) {
    List<String> availableStatements =
        parsedStatements(source).stream().map(TerminalStatement::source).toList();
    return parsedStatements(expectedSource).stream()
        .map(TerminalStatement::source)
        .allMatch(availableStatements::contains);
  }

  private static boolean matchesRequiredCodeLines(
      List<TerminalStatement> statements,
      TerminalCodeRequirement puzzleState,
      TerminalMatchContext context) {
    boolean matched;
    if (puzzleState.hasPrerequisites()) {
      matched = containsLinesWithPrerequisites(statements, puzzleState, context);
    } else if (puzzleState.requiresOrder()) {
      matched = containsCodeLinesInOrder(statements, puzzleState, context);
    } else {
      matched = containsEveryCodeLine(statements, puzzleState.codeLines(), context);
    }
    return matched && puzzleState.captureGroupsAreDistinct(context);
  }

  private static boolean containsLinesWithPrerequisites(
      List<TerminalStatement> statements,
      TerminalCodeRequirement requirement,
      TerminalMatchContext context) {
    boolean[] matchedLines = new boolean[requirement.codeLines().length];
    CodeLine[] requiredLines = requirement.codeLines();
    for (TerminalStatement statement : statements) {
      for (int lineIndex = 0; lineIndex < requiredLines.length; lineIndex++) {
        if (matchedLines[lineIndex]
            || !requirement.prerequisitesMet(lineIndex, matchedLines)) {
          continue;
        }
        TerminalMatchContext candidate = context.copy();
        if (requiredLines[lineIndex].check(statement.source(), candidate)
            && requirement.captureGroupsAreDistinct(candidate)) {
          matchedLines[lineIndex] = true;
          context.replaceWith(candidate);
        }
      }
    }
    return allMatched(matchedLines);
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
      List<TerminalStatement> statements,
      TerminalCodeRequirement requirement,
      TerminalMatchContext context) {
    CodeLine[] codeLines = requirement.codeLines();
    int[] requiredBlockDepths = requirement.requiredBlockDepths();
    for (int startIndex = 0; startIndex < statements.size(); startIndex++) {
      TerminalMatchContext candidate = context.copy();
      if (containsCodeLinesInOrderFrom(
          statements, codeLines, requiredBlockDepths, startIndex, candidate)) {
        context.replaceWith(candidate);
        return true;
      }
    }
    return false;
  }

  private static boolean containsCodeLinesInOrderFrom(
      List<TerminalStatement> statements,
      CodeLine[] codeLines,
      int[] requiredBlockDepths,
      int startIndex,
      TerminalMatchContext context) {
    int nextCodeLineIndex = 0;
    int minimumBlockDepth = 0;
    for (int statementIndex = startIndex; statementIndex < statements.size(); statementIndex++) {
      TerminalStatement statement = statements.get(statementIndex);
      if (requiredBlockDepths != null
          && statement.blockDepth() != requiredBlockDepths[nextCodeLineIndex]) {
        continue;
      }
      if (requiredBlockDepths == null && statement.blockDepth() < minimumBlockDepth) {
        continue;
      }
      TerminalMatchContext candidate = context.copy();
      if (codeLines[nextCodeLineIndex].check(statement.source(), candidate)) {
        context.replaceWith(candidate);
        if (requiredBlockDepths == null && opensControlBlock(statement.source())) {
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
    return matchingStatements(source).stream().map(TerminalStatement::source).toArray(String[]::new);
  }

  /**
   * Resolves local integer variables that cache an array length. This is only a matching view:
   * accepted source is retained verbatim for tracking, quest-log history and save restoration.
   *
   * @param source source text to parse and resolve
   * @return statements with recognized length aliases expanded
   */
  private static List<TerminalStatement> matchingStatements(String source) {
    List<TerminalStatement> statements = parsedStatements(source);
    Map<Integer, String> deferredLengthAliases = new HashMap<>();
    Map<Integer, String> deferredZeroDeclarations = new HashMap<>();
    for (int index = 0; index + 1 < statements.size(); index++) {
      Matcher zero = ZERO_INITIALIZER.matcher(statements.get(index).source());
      if (!zero.matches()) continue;
      TerminalStatement next = statements.get(index + 1);
      Matcher assignment = LENGTH_ASSIGNMENT.matcher(next.source());
      if (next.blockDepth() == statements.get(index).blockDepth()
          && assignment.matches()
          && zero.group("name").equals(assignment.group("name"))
          && aliasIsUsedAfter(statements, index + 2, zero.group("name"), next.blockDepth())) {
        deferredZeroDeclarations.put(index, zero.group("name"));
        deferredLengthAliases.put(index + 1, assignment.group("array"));
      }
    }

    Map<String, List<LengthAlias>> aliases = new HashMap<>();
    List<TerminalStatement> normalized = new ArrayList<>();
    for (int index = 0; index < statements.size(); index++) {
      TerminalStatement statement = statements.get(index);
      aliases.values().forEach(
          bindings -> bindings.removeIf(binding -> binding.blockDepth() > statement.blockDepth()));
      aliases.values().removeIf(List::isEmpty);

      Matcher initializer = LENGTH_INITIALIZER.matcher(statement.source());
      if (initializer.matches()
          && aliasIsUsedAfter(
              statements, index + 1, initializer.group("name"), statement.blockDepth())) {
        bindLengthAlias(
            aliases,
            initializer.group("name"),
            initializer.group("array"),
            statement.blockDepth());
        continue;
      }
      if (deferredZeroDeclarations.containsKey(index)) {
        continue;
      }
      if (deferredLengthAliases.containsKey(index)) {
        bindLengthAlias(
            aliases,
            deferredZeroDeclarations.get(index - 1),
            deferredLengthAliases.get(index),
            statement.blockDepth());
        continue;
      }

      Matcher assignment = VARIABLE_ASSIGNMENT.matcher(statement.source());
      if (assignment.find() && aliases.containsKey(assignment.group("name"))) {
        aliases.remove(assignment.group("name"));
        normalized.add(statement);
        continue;
      }

      Map<String, String> replacements = new HashMap<>();
      aliases.forEach(
          (name, bindings) -> {
            if (!bindings.isEmpty()) {
              replacements.put(name, bindings.get(bindings.size() - 1).arrayLengthExpression());
            }
          });
      normalized.add(
          replacements.isEmpty()
              ? statement
              : new TerminalStatement(
                  replaceIdentifiersOutsideStrings(statement.source(), replacements),
                  statement.blockDepth()));
    }
    return normalized;
  }

  private static boolean aliasIsUsedAfter(
      List<TerminalStatement> statements, int startIndex, String alias, int scopeDepth) {
    for (int index = startIndex; index < statements.size(); index++) {
      TerminalStatement statement = statements.get(index);
      if (statement.blockDepth() < scopeDepth) return false;
      String marker = alias + "__length_alias_reference__";
      if (!replaceIdentifiersOutsideStrings(statement.source(), Map.of(alias, marker))
          .equals(statement.source())) {
        return true;
      }
    }
    return false;
  }

  private static void bindLengthAlias(
      Map<String, List<LengthAlias>> aliases,
      String name,
      String arrayExpression,
      int blockDepth) {
    aliases
        .computeIfAbsent(name, ignored -> new ArrayList<>())
        .add(new LengthAlias(normalizeArrayExpression(arrayExpression) + ".length", blockDepth));
  }

  private static String normalizeArrayExpression(String expression) {
    return expression.replaceAll("\\s*\\[\\s*", "[").replaceAll("\\s*]\\s*", "]");
  }

  private static String replaceIdentifiersOutsideStrings(
      String source, Map<String, String> replacements) {
    StringBuilder result = new StringBuilder(source.length());
    boolean inString = false;
    boolean escaped = false;
    for (int index = 0; index < source.length(); ) {
      char character = source.charAt(index);
      if (inString) {
        result.append(character);
        index++;
        if (escaped) {
          escaped = false;
        } else if (character == '\\') {
          escaped = true;
        } else if (character == '"') {
          inString = false;
        }
        continue;
      }
      if (character == '"') {
        inString = true;
        result.append(character);
        index++;
        continue;
      }
      if (Character.isJavaIdentifierStart(character)) {
        int end = index + 1;
        while (end < source.length() && Character.isJavaIdentifierPart(source.charAt(end))) {
          end++;
        }
        String identifier = source.substring(index, end);
        result.append(replacements.getOrDefault(identifier, identifier));
        index = end;
        continue;
      }
      result.append(character);
      index++;
    }
    return result.toString();
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
        int previousStatementCount = statements.size();
        addStatement(statements, currentStatement, blockDepth);
        if (statements.size() > previousStatementCount
            && opensControlBlock(statements.get(statements.size() - 1).source())) {
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
    return CONTROL_BLOCK_HEADER.matcher(statement).matches();
  }

  private static void addStatement(
      List<TerminalStatement> statements, StringBuilder currentStatement, int blockDepth) {
    String statement = normalizedStatement(currentStatement);
    if (!statement.isBlank() && !statement.equals(";")) {
      statements.add(new TerminalStatement(statement, blockDepth));
    }
    currentStatement.setLength(0);
  }

  private static String normalizedStatement(StringBuilder currentStatement) {
    String statement = currentStatement.toString().trim();
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

  private record LengthAlias(String arrayLengthExpression, int blockDepth) {}

  private record AnalysisResult(boolean successful, TerminalMatchContext context) {}

  private record StateEvaluation(
      EvaluationStatus status,
      TerminalMatchContext context,
      boolean[] matchedLines,
      boolean completeSubmission) {}

  private enum EvaluationStatus {
    COMPLETE,
    PARTIAL,
    NO_PROGRESS,
    INVALID
  }

  /**
   * One source accepted by the shared interpreter at a stable state ID.
   *
   * @param state interpreter state that accepted the source
   * @param source exact source submitted by the player
   */
  public record AcceptedInput(int state, String source) {}
}
