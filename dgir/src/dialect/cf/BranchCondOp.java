package dialect.cf;

import core.ir.Block;
import core.ir.Operation;
import core.ir.Value;
import core.traits.IControlFlow;
import core.traits.ITerminator;
import dialect.builtin.types.IntegerT;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public final class BranchCondOp extends CfOp implements CF, ITerminator, IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "cf.br_cond";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public BranchCondOp() {}

  public BranchCondOp(Operation operation) {
    super(operation);
  }

  public BranchCondOp(Value condition, Block target, Block elseTarget) {
    setOperation(Operation.Create(this, List.of(condition), List.of(target, elseTarget), null));
    assert condition.getType().equals(IntegerT.BOOL) : "Condition must be of type bool/int1.";
  }
}
