package dialect.arith;

/**
 * Sealed marker interface for all operations in the {@link ArithDialect}.
 *
 * <p>Every concrete op must both extend {@link ArithOp} and implement this interface so that
 * {@link core.Utils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface Arith permits ConstantOp, CompareOp, AddOp, SubOp, MulOp, DivOp, RemOp, CastOp {}
