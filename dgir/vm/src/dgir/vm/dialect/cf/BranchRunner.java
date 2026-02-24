package dgir.vm.dialect.cf;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.cf.BranchOp;
import org.jetbrains.annotations.NotNull;

public class BranchRunner extends OpRunner {
  public BranchRunner() {
    super(BranchOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    return Action.Jump(op.getSuccessors().getFirst());
  }
}
