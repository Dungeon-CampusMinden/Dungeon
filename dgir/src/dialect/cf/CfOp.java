package dialect.cf;

import core.Dialect;
import core.ir.Op;
import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all operations in the {@code cf} (control-flow) dialect.
 *
 * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
 * implement {@link CF} to be enumerated by {@link CfDialect}.
 */
public abstract class CfOp extends Op {

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Default constructor used during dialect registration. */
  CfOp() {
    super();
  }

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public CfOp(@NotNull Operation operation) {
    super(operation);
  }

  // =========================================================================
  // Op Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return CfDialect.class;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "cf";
  }
}

