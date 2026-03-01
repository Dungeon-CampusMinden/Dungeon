package dialect.scf;

import core.Dialect;
import core.ir.Op;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all operations in the {@code scf} (structured control flow) dialect.
 *
 * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
 * implement {@link SCF} to be enumerated by {@link SCFDialect}.
 */
public abstract class ScfOp extends Op {

  // =========================================================================
  // Op Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return SCFDialect.class;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "scf";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Default constructor used during dialect registration. */
  ScfOp() {
    super();
  }
}
