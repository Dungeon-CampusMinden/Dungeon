package dialect.func;

import core.Dialect;
import core.ir.Op;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all operations in the {@code func} dialect.
 *
 * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
 * implement {@link Func} to be enumerated by {@link FuncDialect}.
 */
public abstract class FuncBaseOp extends Op {

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Default constructor used during dialect registration. */
  FuncBaseOp() {
    super();
  }

  // =========================================================================
  // Op Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return FuncDialect.class;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "func";
  }
}

