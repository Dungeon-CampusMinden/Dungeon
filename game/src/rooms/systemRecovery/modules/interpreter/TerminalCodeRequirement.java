package rooms.systemRecovery.modules.interpreter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Defines the code required for one interpreter state and its callbacks.
 *
 * @param codeLines code lines that must all be present
 * @param requiresOrder whether the code lines must appear in the registered order
 * @param onSuccess callback for a successful interpretation
 * @param onFailure callback for an unsuccessful interpretation
 * @param acceptsPartialInput whether correct subsets may be accumulated across submissions
 * @param acceptsFollowingStepInSameSubmission whether this submission may complete the next step
 * @param onPartialInput callback for an accepted but incomplete submission
 * @param prerequisites code-line indexes that must already be matched for each code line
 * @param distinctCaptureGroups capture names that must resolve to distinct values
 * @param requiredBlockDepths exact block depth for ordered lines, or {@code null} for legacy
 *     matching
 */
public record TerminalCodeRequirement(
    CodeLine[] codeLines,
    boolean requiresOrder,
    Consumer<TerminalAttempt> onSuccess,
    Consumer<TerminalAttempt> onFailure,
    boolean acceptsPartialInput,
    boolean acceptsFollowingStepInSameSubmission,
    Consumer<TerminalAttempt> onPartialInput,
    int[][] prerequisites,
    List<Set<String>> distinctCaptureGroups,
    int[] requiredBlockDepths) {

  /**
   * Creates the standard all-or-nothing requirement for one terminal step.
   *
   * @param codeLines code lines that must all be present
   * @param requiresOrder whether the code lines must appear in the registered order
   * @param onSuccess callback for a successful interpretation
   * @param onFailure callback for an unsuccessful interpretation
   */
  public TerminalCodeRequirement(
      CodeLine[] codeLines,
      boolean requiresOrder,
      Consumer<TerminalAttempt> onSuccess,
      Consumer<TerminalAttempt> onFailure) {
    this(codeLines, requiresOrder, onSuccess, onFailure, false, false, null, null, List.of(), null);
  }

  /** Creates a requirement using the previous extended constructor shape. */
  public TerminalCodeRequirement(
      CodeLine[] codeLines,
      boolean requiresOrder,
      Consumer<TerminalAttempt> onSuccess,
      Consumer<TerminalAttempt> onFailure,
      boolean acceptsPartialInput,
      boolean acceptsFollowingStepInSameSubmission,
      Consumer<TerminalAttempt> onPartialInput) {
    this(
        codeLines,
        requiresOrder,
        onSuccess,
        onFailure,
        acceptsPartialInput,
        acceptsFollowingStepInSameSubmission,
        onPartialInput,
        null,
        List.of(),
        null);
  }

  /** Creates a requirement using the previous prerequisites-and-captures constructor shape. */
  public TerminalCodeRequirement(
      CodeLine[] codeLines,
      boolean requiresOrder,
      Consumer<TerminalAttempt> onSuccess,
      Consumer<TerminalAttempt> onFailure,
      boolean acceptsPartialInput,
      boolean acceptsFollowingStepInSameSubmission,
      Consumer<TerminalAttempt> onPartialInput,
      int[][] prerequisites,
      List<Set<String>> distinctCaptureGroups) {
    this(
        codeLines,
        requiresOrder,
        onSuccess,
        onFailure,
        acceptsPartialInput,
        acceptsFollowingStepInSameSubmission,
        onPartialInput,
        prerequisites,
        distinctCaptureGroups,
        null);
  }

  /**
   * Creates an unordered terminal puzzle step.
   *
   * @param codeLines code lines that must all be present
   * @param onSuccess callback for a successful interpretation
   * @param onFailure callback for an unsuccessful interpretation
   */
  public TerminalCodeRequirement(CodeLine[] codeLines, Runnable onSuccess, Runnable onFailure) {
    this(
        codeLines,
        false,
        ignored -> {
          if (onSuccess != null) onSuccess.run();
        },
        ignored -> {
          if (onFailure != null) onFailure.run();
        },
        false,
        false,
        null,
        null,
        List.of(),
        null);
  }

  /**
   * Creates an immutable terminal puzzle step.
   *
   * @param codeLines code lines that must all be present
   * @param requiresOrder whether the code lines must appear in the registered order
   * @param onSuccess callback for a successful interpretation
   * @param onFailure callback for an unsuccessful interpretation
   */
  public TerminalCodeRequirement {
    codeLines = Arrays.copyOf(codeLines, codeLines.length);
    onSuccess = onSuccess == null ? ignored -> {} : onSuccess;
    onFailure = onFailure == null ? ignored -> {} : onFailure;
    onPartialInput = onPartialInput == null ? ignored -> {} : onPartialInput;
    prerequisites = copyPrerequisites(prerequisites, codeLines.length);
    distinctCaptureGroups =
        distinctCaptureGroups == null
            ? List.of()
            : distinctCaptureGroups.stream().map(Set::copyOf).toList();
    if (requiredBlockDepths != null) {
      if (!requiresOrder || requiredBlockDepths.length != codeLines.length) {
        throw new IllegalArgumentException(
            "Exact block depths require ordered code lines and one depth per line.");
      }
      requiredBlockDepths = Arrays.copyOf(requiredBlockDepths, requiredBlockDepths.length);
      if (Arrays.stream(requiredBlockDepths).anyMatch(depth -> depth < 0)) {
        throw new IllegalArgumentException("Block depths cannot be negative.");
      }
    }
  }

  /**
   * Returns a copy that accumulates matched code lines across multiple submissions.
   *
   * @param callback callback for a valid but incomplete submission
   * @return configured requirement
   */
  public TerminalCodeRequirement withPartialSubmissions(Consumer<TerminalAttempt> callback) {
    return new TerminalCodeRequirement(
        codeLines,
        requiresOrder,
        onSuccess,
        onFailure,
        true,
        acceptsFollowingStepInSameSubmission,
        callback,
        prerequisites,
        distinctCaptureGroups,
        requiredBlockDepths);
  }

  /**
   * Returns a copy that may also accept statements belonging to the immediately following step.
   *
   * @return configured requirement
   */
  public TerminalCodeRequirement allowingFollowingStepInSameSubmission() {
    return new TerminalCodeRequirement(
        codeLines,
        requiresOrder,
        onSuccess,
        onFailure,
        acceptsPartialInput,
        true,
        onPartialInput,
        prerequisites,
        distinctCaptureGroups,
        requiredBlockDepths);
  }

  /**
   * Returns a copy whose lines require the specified earlier lines to have matched first.
   *
   * @param linePrerequisites prerequisite indexes for each code line
   * @return configured requirement
   */
  public TerminalCodeRequirement requiringPrerequisites(int[][] linePrerequisites) {
    return new TerminalCodeRequirement(
        codeLines,
        requiresOrder,
        onSuccess,
        onFailure,
        acceptsPartialInput,
        acceptsFollowingStepInSameSubmission,
        onPartialInput,
        linePrerequisites,
        distinctCaptureGroups,
        requiredBlockDepths);
  }

  /**
   * Returns a copy whose named captures must be different within each supplied group.
   *
   * @param captureGroups capture names that must not have duplicate values
   * @return configured requirement
   */
  public TerminalCodeRequirement requiringDistinctCaptures(List<Set<String>> captureGroups) {
    return new TerminalCodeRequirement(
        codeLines,
        requiresOrder,
        onSuccess,
        onFailure,
        acceptsPartialInput,
        acceptsFollowingStepInSameSubmission,
        onPartialInput,
        prerequisites,
        captureGroups,
        requiredBlockDepths);
  }

  /**
   * Returns a copy requiring each ordered line at the specified nesting depth.
   *
   * @param blockDepths nesting depth for each code line
   * @return configured requirement
   */
  public TerminalCodeRequirement requiringBlockDepths(int... blockDepths) {
    return new TerminalCodeRequirement(
        codeLines,
        requiresOrder,
        onSuccess,
        onFailure,
        acceptsPartialInput,
        acceptsFollowingStepInSameSubmission,
        onPartialInput,
        prerequisites,
        distinctCaptureGroups,
        blockDepths);
  }

  boolean prerequisitesMet(int lineIndex, boolean[] matchedLines) {
    for (int prerequisite : prerequisites[lineIndex]) {
      if (prerequisite < 0 || prerequisite >= matchedLines.length || !matchedLines[prerequisite]) {
        return false;
      }
    }
    return true;
  }

  boolean hasPrerequisites() {
    return Arrays.stream(prerequisites).anyMatch(linePrerequisites -> linePrerequisites.length > 0);
  }

  boolean captureGroupsAreDistinct(TerminalMatchContext context) {
    return distinctCaptureGroups.stream().allMatch(context::areDistinct);
  }

  /**
   * Returns a copy of the required code lines.
   *
   * @return registered code lines
   */
  @Override
  public CodeLine[] codeLines() {
    return Arrays.copyOf(codeLines, codeLines.length);
  }

  /** Returns a defensive copy of the line prerequisites. */
  @Override
  public int[][] prerequisites() {
    return copyPrerequisites(prerequisites, codeLines.length);
  }

  /** Returns a defensive copy of the required block depths, if configured. */
  @Override
  public int[] requiredBlockDepths() {
    return requiredBlockDepths == null
        ? null
        : Arrays.copyOf(requiredBlockDepths, requiredBlockDepths.length);
  }

  private static int[][] copyPrerequisites(int[][] source, int lineCount) {
    if (source == null) {
      int[][] empty = new int[lineCount][];
      Arrays.setAll(empty, ignored -> new int[0]);
      return empty;
    }
    if (source.length != lineCount) {
      throw new IllegalArgumentException("There must be one prerequisite list per code line.");
    }
    return Arrays.stream(source).map(int[]::clone).toArray(int[][]::new);
  }
}
