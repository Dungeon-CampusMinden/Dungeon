package dialect.scf;

import core.ir.Operation;
import core.traits.IControlFlow;
import core.traits.ISingleRegion;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Op which opens a new scope. The scope has no effect other than hiding new variables from the
 * outside.
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

  public ScopeOp() {
    executeIfRegistered(
        ScopeOp.class, () -> setOperation(true, Operation.Create(this, null, null, null, 1)));
  }

  public ScopeOp(Operation operation) {
    super(operation);
  }
}
