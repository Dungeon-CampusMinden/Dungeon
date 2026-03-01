package dialect.arith;

import core.Dialect;
import core.ir.Op;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all operations in the {@code arith} (arithmetic) dialect.
 *
 * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
 * implement {@link Arith} to be enumerated by {@link ArithDialect}.
 */
public abstract class ArithOp extends Op {

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Default constructor used during dialect registration. */
  ArithOp() {
    super();
  }

  // =========================================================================
  // Op Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return ArithDialect.class;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "arith";
  }
}

