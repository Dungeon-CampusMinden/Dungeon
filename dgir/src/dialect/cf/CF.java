package dialect.cf;

/**
 * Sealed marker interface for all operations in the {@link CfDialect}.
 *
 * <p>Every concrete op must both extend {@link CfOp} and implement this interface so that
 * {@link core.Utils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface CF permits BranchOp, BranchCondOp {}
