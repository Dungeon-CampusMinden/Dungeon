package dialect.io;

import core.Dialect;
import core.ir.Op;
import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all operations in the {@code io} dialect.
 *
 * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
 * implement {@link IO} to be enumerated by {@link IoDialect}.
 */
public abstract class IoOp extends Op {

  /** Default constructor used during dialect registration. */
  IoOp() {
    super();
  }

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public IoOp(Operation operation) {
    super(operation);
  }

  @Contract(pure = true)
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return IoDialect.class;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "io";
  }
}
