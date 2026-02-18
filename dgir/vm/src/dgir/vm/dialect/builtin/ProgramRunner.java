package dgir.vm.dialect.builtin;

import core.detail.RegisteredOperationDetails;
import core.ir.Op;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.builtin.ProgramOp;
import dialect.func.FuncOp;
import org.jetbrains.annotations.NotNull;

public class ProgramRunner extends OpRunner {
  public ProgramRunner() {
    super(RegisteredOperationDetails.get(ProgramOp.class));
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    ProgramOp programOp = op.as(ProgramOp.class);
    // Find the entry function and jump to it.
    FuncOp entry = null;
    for (Operation operation : programOp.getBlock().getOperations()) {
      FuncOp funcOp = operation.as(FuncOp.class);
      if (funcOp != null && funcOp.getFuncName().equals("main")) {
        entry = funcOp;
        break;
      }
    }
    assert entry != null : "Could not find entry function. This should have been caught by verification.";

    return Action.Call(entry.getOperation());
  }
}
