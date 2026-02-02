package blockly.vm.dgir.core;

/**
 * Result values for AST walking methods.
 */
public enum WalkResult {
  /** Continue walking as normal */
  CONTINUE,
  /** Skip walking the children of the current node */
  SKIP_CHILDREN,
  /** Abort the entire walk immediately */
  ABORT
}
