package dialect.cf;

import core.ir.Block;
import core.ir.Operation;
import core.traits.IControlFlow;
import core.traits.ITerminator;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public final class BranchOp extends CfOp implements CF, ITerminator, IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "cf.br";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  private BranchOp() {}

  public BranchOp(Operation operation) {
    super(operation);
  }

  public BranchOp(Block target) {
    setOperation(Operation.Create(this, null, List.of(target), null));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public Block getTarget() {
    return getSuccessors().getFirst();
  }
}
