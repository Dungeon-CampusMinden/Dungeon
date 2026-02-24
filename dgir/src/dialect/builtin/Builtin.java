package dialect.builtin;

/**
 * Sealed marker interface for all operations in the {@link BuiltinDialect}.
 *
 * <p>Every concrete op must both extend {@link BuiltinOp} and implement this interface so that
 * {@link core.Utils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface Builtin permits ProgramOp {}
