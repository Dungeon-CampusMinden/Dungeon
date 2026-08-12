package foundation.definition;

/** Closed classification of how directly an authored hint discloses help. */
public enum HintSeverity {
  /** Explains the task or gives a starting point. */
  ORIENTATION,
  /** Explains an approach without directly stating the solution. */
  APPROACH,
  /** Directly states the solution. */
  SOLUTION
}
