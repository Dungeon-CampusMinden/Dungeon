package dialect.scf;

import core.ir.Operation;
import core.debug.Location;
import core.traits.IControlFlow;
import core.traits.ISingleRegion;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Opens a new lexical scope in the {@code scf} dialect.
 *
 * <p>A {@code ScopeOp} has no semantic effect other than restricting the visibility of values
 * defined inside it — they are not accessible outside the scope's single region. The verifier
 * always passes.
 *
 * <p>Ident: {@code scf.scope}
 *
 * <pre>{@code
 * scf.scope {
 *   // variables declared here are not visible outside
 * }
 * }</pre>
 */
public final class ScopeOp extends ScfOp implements SCF, ISingleRegion, IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "scf.scope";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Default constructor used during dialect registration. */
  private ScopeOp() {}

  /**
   * Create a scope op.
   *
   * @param location the source location of this operation.
   */
  public ScopeOp(@NotNull Location location) {
    setOperation(true, Operation.Create(location, this, null, null, null, 1));
  }
}
