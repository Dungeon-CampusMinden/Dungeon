package dgir.vm.dialect.cf;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import static dialect.cf.CfOps.BranchCondOp;

public class BranchCondRunner extends OpRunner {
  public BranchCondRunner() {
    super(BranchCondOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    BranchCondOp branchCondOp = op.as(BranchCondOp.class).orElseThrow();
    byte condition =
        state.getValue(branchCondOp.getOperand(0).orElseThrow(), Byte.class).orElseThrow();
    if (condition != 0) {
      return Action.Jump(op.getSuccessors().get(0));
    } else {
      return Action.Jump(op.getSuccessors().get(1));
    }
  }
}
