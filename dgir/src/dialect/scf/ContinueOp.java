package dialect.scf;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.Op;
import core.ir.Operation;
import core.traits.ISpecificParentOp;
import core.traits.ITerminator;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/** Marks the end of a structured control flow region. */
public final class ContinueOp extends ScfOp implements SCF, ITerminator, ISpecificParentOp {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "scf.continue";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public ContinueOp() {
    executeIfRegistered(
        ContinueOp.class, () -> setOperation(true, Operation.Create(this, null, null, null)));
  }

  public ContinueOp(Operation operation) {
    super(operation);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Class<? extends Op>> getValidParentTypes() {
    return List.of(IfOp.class, ScopeOp.class, ForOp.class);
  }
}
