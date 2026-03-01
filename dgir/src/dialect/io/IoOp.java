package dialect.io;

import core.Dialect;
import core.ir.Op;
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
