package dialect.builtin;

import core.Dialect;
import core.ir.Op;
import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all operations in the {@code builtin} dialect (namespace {@code ""}).
 *
 * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
 * implement {@link Builtin} to be enumerated by {@link BuiltinDialect}.
 */
public abstract class BuiltinOp extends Op {

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Default constructor used during dialect registration. */
  BuiltinOp() {
    super();
  }

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public BuiltinOp(@NotNull Operation operation) {
    super(operation);
  }

  // =========================================================================
  // Op Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return BuiltinDialect.class;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "";
  }
}

