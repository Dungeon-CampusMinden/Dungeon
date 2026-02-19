package dgir.vm.dialect.cf;

import core.detail.RegisteredOperationDetails;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.cf.BranchCondOp;
import org.jetbrains.annotations.NotNull;

public class BranchCondRunner extends OpRunner {
  public BranchCondRunner() {
    super(RegisteredOperationDetails.lookup(BranchCondOp.class).orElseThrow());
  }


  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    BranchCondOp branchCondOp = op.as(BranchCondOp.class).orElseThrow();
    byte condition = state.getValue(branchCondOp.getOperands().getFirst().getValue(), Byte.class).orElseThrow();
    if (condition != 0) {
      return Action.Jump(op.getSuccessors().get(0));
    } else {
      return Action.Jump(op.getSuccessors().get(1));
    }
  }
}
