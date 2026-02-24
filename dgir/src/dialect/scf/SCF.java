package dialect.scf;

/**
 * Sealed marker interface for all operations in the {@link SCFDialect}.
 *
 * <p>Every concrete op must both extend {@link ScfOp} and implement this interface so that
 * {@link core.Utils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface SCF permits BreakOp, ContinueOp, ForOp, IfOp, ScopeOp {}
