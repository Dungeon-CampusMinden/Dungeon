package core;

/** Result values returned by AST/IR walking callbacks to control traversal flow. */
public enum WalkResult {

  // =========================================================================
  // Values
  // =========================================================================

  /** Continue walking normally. */
  CONTINUE,

  /** Skip the children of the current node and continue with its siblings. */
  SKIP_CHILDREN,

  /** Abort the entire walk immediately. */
  ABORT
}
