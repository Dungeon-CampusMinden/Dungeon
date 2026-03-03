package dgir.vm.dialect.cf;

import static dialect.cf.CfOps.BranchOp;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

public class BranchRunner extends OpRunner {
  public BranchRunner() {
    super(BranchOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    return Action.JumpToBlock(op.getSuccessors().getFirst());
  }
}
