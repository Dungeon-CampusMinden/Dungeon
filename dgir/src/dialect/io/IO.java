package dialect.io;

/**
 * Sealed marker interface for all operations in the {@link IoDialect}.
 *
 * <p>Every concrete op must both extend {@link IoOp} and implement this interface so that
 * {@link core.Utils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface IO permits ConsoleInOp, PrintOp {}
